package com.justice.playswap.spotify.client;

import com.justice.playswap.spotify.model.SpotifyPlaylist;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class SpotifyApiClient {

    private final WebClient.Builder webClientBuilder;

    private static final String SPOTIFY_API_BASE = "https://api.spotify.com/v1";

    public Mono<List<SpotifyPlaylist>> getUserPlaylists(String accessToken) {
        return getCurrentSpotifyUserId(accessToken)
                .flatMap(currentUserId -> {
                    log.info("==> [SpotifyApiClient] Current user Spotify ID: '{}'", currentUserId);

                    return webClientBuilder.build()
                            .get()
                            .uri(SPOTIFY_API_BASE + "/me/playlists?limit=50")
                            .header("Authorization", "Bearer " + accessToken)
                            .retrieve()
                            .bodyToMono(Map.class)
                            .retryWhen(com.justice.playswap.spotify.util.SpotifyRetryUtil.rateLimitRetrySpec())
                            .map(response -> {
                                if (response == null || response.get("items") == null) {
                                    return List.<SpotifyPlaylist>of();
                                }

                                List<Map<String, Object>> items =
                                        (List<Map<String, Object>>) response.get("items");

                                return items.stream()
                                        .filter(Objects::nonNull)
                                        .filter(item -> isCreatedByCurrentUser(item, currentUserId))
                                        .map(item -> {
                                            SpotifyPlaylist playlist = new SpotifyPlaylist();
                                            playlist.setId((String) item.get("id"));
                                            playlist.setName((String) item.get("name"));
                                            playlist.setDescription((String) item.get("description"));

                                            int totalTracks = parseTotalTracks(item);
                                            playlist.setTotalTracks(totalTracks);
                                            log.debug("Fetched user-owned Spotify playlist '{}' (id: {}) with {} total tracks", playlist.getName(), playlist.getId(), totalTracks);

                                            List<Map<String, Object>> images =
                                                    (List<Map<String, Object>>) item.get("images");
                                            if (images != null && !images.isEmpty() && images.get(0) != null) {
                                                playlist.setImageUrl((String) images.get(0).get("url"));
                                            }

                                            return playlist;
                                        }).collect(Collectors.toList());
                            })
                            .doOnNext(playlists -> log.info("Successfully fetched {} user-owned Spotify playlists", playlists.size()));
                })
                .doOnError(e -> log.error("Failed to fetch Spotify playlists: {}", e.getMessage()));
    }

    @SuppressWarnings("unchecked")
    public Mono<com.justice.playswap.spotify.model.CreatePlaylistResponse> createPlaylist(String name, String description, String accessToken) {
        Map<String, Object> body = Map.of(
                "name", name != null ? name : "Transferred Playlist",
                "description", description != null ? description : "Transferred via PlaySwap",
                "public", false
        );

        return webClientBuilder.build()
                .post()
                .uri(SPOTIFY_API_BASE + "/me/playlists")
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .retryWhen(com.justice.playswap.spotify.util.SpotifyRetryUtil.rateLimitRetrySpec())
                .map(response -> {
                    String id = (String) response.get("id");
                    String playlistName = (String) response.get("name");
                    String url = "";
                    Map<String, Object> externalUrls = (Map<String, Object>) response.get("external_urls");
                    if (externalUrls != null) {
                        url = (String) externalUrls.get("spotify");
                    }
                    log.info("Successfully created Spotify playlist '{}' (ID: {})", playlistName, id);
                    return com.justice.playswap.spotify.model.CreatePlaylistResponse.builder()
                            .playlistId(id)
                            .name(playlistName)
                            .playlistUrl(url != null ? url : "https://open.spotify.com/playlist/" + id)
                            .build();
                })
                .doOnError(e -> log.error("Failed to create Spotify playlist '{}': {}", name, e.getMessage()));
    }

    public Mono<Boolean> addTracksToPlaylist(String playlistId, List<String> trackIds, String accessToken) {
        if (trackIds == null || trackIds.isEmpty()) {
            return Mono.just(true);
        }

        List<String> uris = trackIds.stream()
                .filter(id -> id != null && !id.isBlank())
                .map(id -> id.startsWith("spotify:track:") ? id : "spotify:track:" + id)
                .collect(Collectors.toList());

        if (uris.isEmpty()) {
            return Mono.just(true);
        }

        List<List<String>> batches = new ArrayList<>();
        for (int i = 0; i < uris.size(); i += 100) {
            batches.add(uris.subList(i, Math.min(i + 100, uris.size())));
        }

        return reactor.core.publisher.Flux.fromIterable(batches)
                .concatMap(batchUris -> webClientBuilder.build()
                        .post()
                        .uri(SPOTIFY_API_BASE + "/playlists/" + playlistId + "/tracks")
                        .header("Authorization", "Bearer " + accessToken)
                        .header("Content-Type", "application/json")
                        .bodyValue(Map.of("uris", batchUris))
                        .retrieve()
                        .bodyToMono(Map.class)
                        .retryWhen(com.justice.playswap.spotify.util.SpotifyRetryUtil.rateLimitRetrySpec())
                        .doOnNext(res -> log.info("Successfully added {} tracks to Spotify playlist {}", batchUris.size(), playlistId))
                )
                .then(Mono.just(true))
                .doOnError(e -> log.error("Failed to add tracks to Spotify playlist {}: {}", playlistId, e.getMessage()))
                .onErrorReturn(false);
    }

    private Mono<String> getCurrentSpotifyUserId(String accessToken) {
        return webClientBuilder.build()
                .get()
                .uri(SPOTIFY_API_BASE + "/me")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .retryWhen(com.justice.playswap.spotify.util.SpotifyRetryUtil.rateLimitRetrySpec())
                .map(response -> response != null ? (String) response.get("id") : "")
                .onErrorReturn("");
    }

    @SuppressWarnings("unchecked")
    private boolean isCreatedByCurrentUser(Map<String, Object> item, String currentUserId) {
        if (currentUserId == null || currentUserId.isEmpty()) {
            return true;
        }
        Object ownerObj = item.get("owner");
        if (ownerObj instanceof Map<?, ?> ownerMap) {
            String ownerId = (String) ownerMap.get("id");
            return currentUserId.equalsIgnoreCase(ownerId);
        }
        return true;
    }

    private int parseTotalTracks(Map<String, Object> item) {
        if (item == null) return 0;

        // 1. Check "tracks" object
        Object tracksObj = item.get("tracks");
        if (tracksObj != null) {
            int count = extractCountFromObj(tracksObj);
            if (count > 0) return count;
        }

        // 2. Check "items" object inside playlist item
        Object itemsObj = item.get("items");
        if (itemsObj != null) {
            int count = extractCountFromObj(itemsObj);
            if (count > 0) return count;
        }

        // 3. Direct checks on item
        for (String key : List.of("totalTracks", "total_tracks", "tracksCount", "tracks_count", "itemCount", "item_count", "total")) {
            int count = parseNumber(item.get(key));
            if (count > 0) return count;
        }

        return 0;
    }

    private int extractCountFromObj(Object obj) {
        if (obj instanceof Map<?, ?> map) {
            for (String key : List.of("total", "totalTracks", "total_tracks", "count", "itemCount", "item_count", "length")) {
                int count = parseNumber(map.get(key));
                if (count > 0) return count;
            }
            if (map.get("items") instanceof List<?> list) {
                return list.size();
            }
        } else if (obj instanceof List<?> list) {
            return list.size();
        } else {
            return parseNumber(obj);
        }
        return 0;
    }

    private int parseNumber(Object val) {
        if (val == null) return 0;
        if (val instanceof Number num) {
            return num.intValue();
        }
        if (val instanceof String str) {
            try {
                return Integer.parseInt(str.trim());
            } catch (NumberFormatException ignored) {}
        }
        return 0;
    }

    public Mono<List<com.justice.playswap.spotify.model.SpotifyTrack>> getPlaylistTracks(String playlistId, String accessToken) {
        String itemsUrl = SPOTIFY_API_BASE + "/playlists/" + playlistId + "/items?limit=100&market=from_token";
        log.info("==> [SpotifyApiClient] Fetching tracks for playlist {} using primary /items endpoint: {}", playlistId, itemsUrl);

        return fetchTracksRecursive(itemsUrl, accessToken)
                .flatMap(tracks -> {
                    if (!tracks.isEmpty()) {
                        return Mono.just(tracks);
                    }
                    log.info("Primary /items endpoint with market=from_token returned empty for playlist {}, trying /items without market param...", playlistId);
                    String fallbackItemsUrl = SPOTIFY_API_BASE + "/playlists/" + playlistId + "/items?limit=100";
                    return fetchTracksRecursive(fallbackItemsUrl, accessToken);
                })
                .flatMap(tracks -> {
                    if (!tracks.isEmpty()) {
                        return Mono.just(tracks);
                    }
                    log.info("Fallback /items endpoint returned empty for playlist {}, trying legacy /tracks endpoint...", playlistId);
                    String tracksUrl = SPOTIFY_API_BASE + "/playlists/" + playlistId + "/tracks?limit=100&market=from_token";
                    return fetchTracksRecursive(tracksUrl, accessToken);
                })
                .flatMap(tracks -> {
                    if (!tracks.isEmpty()) {
                        return Mono.just(tracks);
                    }
                    log.info("Legacy /tracks endpoint returned empty for playlist {}, attempting direct playlist endpoint fallback...", playlistId);
                    return fetchPlaylistDirect(playlistId, accessToken);
                })
                .onErrorResume(e -> {
                    log.warn("Primary /items fetch failed for playlist {} ({}), attempting fallback without market param...", playlistId, e.getMessage());
                    String fallbackItemsUrl = SPOTIFY_API_BASE + "/playlists/" + playlistId + "/items?limit=100";
                    return fetchTracksRecursive(fallbackItemsUrl, accessToken)
                            .onErrorResume(ex -> {
                                log.warn("Fallback /items endpoint failed for playlist {}, trying /tracks endpoint...", playlistId);
                                String tracksUrl = SPOTIFY_API_BASE + "/playlists/" + playlistId + "/tracks?limit=100";
                                return fetchTracksRecursive(tracksUrl, accessToken)
                                        .onErrorResume(ex2 -> {
                                            log.warn("Fallback /tracks endpoint failed for playlist {}, trying direct playlist endpoint...", playlistId);
                                            return fetchPlaylistDirect(playlistId, accessToken);
                                        });
                            });
                });
    }

    private Mono<List<com.justice.playswap.spotify.model.SpotifyTrack>> fetchPlaylistDirect(String playlistId, String accessToken) {
        String url = SPOTIFY_API_BASE + "/playlists/" + playlistId;
        log.info("==> [SpotifyApiClient] Fallback: Calling Spotify Direct Playlist URL: '{}'", url);

        return webClientBuilder.build()
                .get()
                .uri(url)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .retryWhen(com.justice.playswap.spotify.util.SpotifyRetryUtil.rateLimitRetrySpec())
                .map(response -> {
                    if (response == null || response.get("tracks") == null) {
                        return List.<com.justice.playswap.spotify.model.SpotifyTrack>of();
                    }
                    Map<String, Object> tracksObj = (Map<String, Object>) response.get("tracks");
                    if (tracksObj == null || tracksObj.get("items") == null) {
                        return List.<com.justice.playswap.spotify.model.SpotifyTrack>of();
                    }
                    List<Map<String, Object>> items = (List<Map<String, Object>>) tracksObj.get("items");
                    return items.stream()
                            .filter(Objects::nonNull)
                            .map(this::extractTrackObj)
                            .filter(Objects::nonNull)
                            .map(this::normalizeSpotifyTrack)
                            .filter(track -> track != null && track.getTrackId() != null)
                            .collect(Collectors.toList());
                })
                .doOnError(e -> {
                    if (e instanceof org.springframework.web.reactive.function.client.WebClientResponseException wre) {
                        log.error("❌ Direct playlist fallback failed for playlist {}. Status: {}, Body: {}", playlistId, wre.getStatusCode(), wre.getResponseBodyAsString());
                    } else {
                        log.error("❌ Direct playlist fallback failed for playlist {}: {}", playlistId, e.getMessage());
                    }
                })
                .onErrorReturn(List.of());
    }

    private Mono<List<com.justice.playswap.spotify.model.SpotifyTrack>> fetchTracksRecursive(String url, String accessToken) {
        if (url == null || url.isEmpty()) {
            return Mono.just(List.of());
        }

        log.info("==> [SpotifyApiClient] Calling Spotify Web API URL: '{}' | Authorization: Bearer '{}...'",
                url, accessToken != null && accessToken.length() > 15 ? accessToken.substring(0, 15) : accessToken);

        return webClientBuilder.build()
                .get()
                .uri(url)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .retryWhen(com.justice.playswap.spotify.util.SpotifyRetryUtil.rateLimitRetrySpec())
                .flatMap(response -> {
                    if (response == null || response.get("items") == null) {
                        return Mono.just(List.<com.justice.playswap.spotify.model.SpotifyTrack>of());
                    }

                    List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
                    List<com.justice.playswap.spotify.model.SpotifyTrack> pageTracks = items.stream()
                            .filter(Objects::nonNull)
                            .map(this::extractTrackObj)
                            .filter(Objects::nonNull)
                            .map(this::normalizeSpotifyTrack)
                            .filter(track -> track != null && track.getTrackId() != null)
                            .collect(Collectors.toList());

                    String nextUrl = (String) response.get("next");
                    if (nextUrl != null && !nextUrl.isEmpty()) {
                        return fetchTracksRecursive(nextUrl, accessToken)
                                .map(nextTracks -> {
                                    pageTracks.addAll(nextTracks);
                                    return pageTracks;
                                });
                    }

                    return Mono.just(pageTracks);
                })
                .doOnError(e -> {
                    if (e instanceof org.springframework.web.reactive.function.client.WebClientResponseException wre) {
                        log.error("Failed to fetch Spotify playlist tracks from {}. Status: {}, Body: {}", url, wre.getStatusCode(), wre.getResponseBodyAsString());
                    } else {
                        log.error("Failed to fetch Spotify playlist tracks from {}: {}", url, e.getMessage());
                    }
                });
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractTrackObj(Map<String, Object> item) {
        if (item == null) return null;
        if (item.get("track") instanceof Map<?, ?> trackMap) {
            return (Map<String, Object>) trackMap;
        }
        if (item.get("item") instanceof Map<?, ?> itemMap) {
            return (Map<String, Object>) itemMap;
        }
        if (item.containsKey("id") || item.containsKey("name") || item.containsKey("uri")) {
            return item;
        }
        return null;
    }

    private com.justice.playswap.spotify.model.SpotifyTrack normalizeSpotifyTrack(Map<String, Object> trackMap) {
        if (trackMap == null) return null;
        String id = (String) trackMap.get("id");
        if (id == null && trackMap.get("linked_from") instanceof Map<?, ?> linkedFrom) {
            id = (String) linkedFrom.get("id");
        }
        if (id == null && trackMap.get("uri") instanceof String uri) {
            if (uri.startsWith("spotify:track:")) {
                id = uri.substring("spotify:track:".length());
            }
        }
        String name = (String) trackMap.get("name");
        if (id == null || name == null) return null;

        List<Map<String, Object>> artistsList = (List<Map<String, Object>>) trackMap.get("artists");
        String artists = "";
        String primaryArtist = "";
        if (artistsList != null && !artistsList.isEmpty()) {
            artists = artistsList.stream()
                    .map(a -> (String) a.get("name"))
                    .filter(a -> a != null)
                    .collect(Collectors.joining(", "));
            primaryArtist = (String) artistsList.get(0).get("name");
        }

        String albumName = "";
        Map<String, Object> albumMap = (Map<String, Object>) trackMap.get("album");
        if (albumMap != null) {
            albumName = (String) albumMap.get("name");
        }

        long durationMs = parseNumber(trackMap.get("duration_ms"));
        String isrc = null;
        Map<String, Object> externalIds = (Map<String, Object>) trackMap.get("external_ids");
        if (externalIds != null) {
            isrc = (String) externalIds.get("isrc");
        }

        return com.justice.playswap.spotify.model.SpotifyTrack.builder()
                .trackId(id)
                .title(name)
                .artists(artists)
                .primaryArtist(primaryArtist != null && !primaryArtist.isEmpty() ? primaryArtist : artists)
                .album(albumName)
                .durationMs(durationMs)
                .isrc(isrc)
                .build();
    }
}
