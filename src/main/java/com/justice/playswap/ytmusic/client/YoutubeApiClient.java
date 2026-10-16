package com.justice.playswap.ytmusic.client;

import com.justice.playswap.ytmusic.model.YoutubePlaylist;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class YoutubeApiClient {

    private final WebClient.Builder webClientBuilder;

    private static final String YOUTUBE_API_BASE =
            "https://www.googleapis.com/youtube/v3";

    public Mono<List<YoutubePlaylist>> getUserPlaylists(String accessToken) {
        return webClientBuilder.build()
                .get()
                .uri(YOUTUBE_API_BASE +
                        "/playlists?part=snippet,contentDetails&mine=true&maxResults=50")
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    if (response == null || response.get("items") == null) {
                        return List.<YoutubePlaylist>of();
                    }

                    List<Map<String, Object>> items =
                            (List<Map<String, Object>>) response.get("items");

                    return items.stream()
                            .filter(item -> item != null)
                            .map(item -> {
                                YoutubePlaylist playlist = new YoutubePlaylist();

                                // ID
                                playlist.setId((String) item.get("id"));

                                // Snippet contains name, description, thumbnail
                                Map<String, Object> snippet =
                                        (Map<String, Object>) item.get("snippet");
                                if (snippet != null) {
                                    playlist.setName((String) snippet.get("title"));
                                    playlist.setDescription((String) snippet.get("description"));

                                    // Thumbnail
                                    Map<String, Object> thumbnails =
                                            (Map<String, Object>) snippet.get("thumbnails");
                                    if (thumbnails != null) {
                                        Map<String, Object> defaultThumb =
                                                (Map<String, Object>) thumbnails.get("default");
                                        if (defaultThumb != null) {
                                            playlist.setImageUrl(
                                                    (String) defaultThumb.get("url"));
                                        }
                                    }
                                }

                                // Total tracks
                                int totalTracks = parseTotalTracks(item);
                                playlist.setTotalTracks(totalTracks);
                                log.info("Fetched YouTube playlist '{}' (id: {}) with {} total tracks", playlist.getName(), playlist.getId(), totalTracks);

                                return playlist;
                            }).collect(Collectors.toList());
                })
                .doOnError(e -> log.error(
                        "Failed to fetch YouTube playlists: {}", e.getMessage()));
    }

    @SuppressWarnings("unchecked")
    public Mono<com.justice.playswap.ytmusic.model.CreatePlaylistResponse> createPlaylist(String name, String description, String accessToken) {
        Map<String, Object> body = Map.of(
                "snippet", Map.of(
                        "title", name != null ? name : "Transferred Playlist",
                        "description", description != null ? description : "Transferred via PlaySwap"
                ),
                "status", Map.of(
                        "privacyStatus", "private"
                )
        );

        return webClientBuilder.build()
                .post()
                .uri(YOUTUBE_API_BASE + "/playlists?part=snippet,status")
                .header("Authorization", "Bearer " + accessToken)
                .header("Content-Type", "application/json")
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    String id = (String) response.get("id");
                    String playlistName = name;
                    Map<String, Object> snippet = (Map<String, Object>) response.get("snippet");
                    if (snippet != null && snippet.get("title") != null) {
                        playlistName = (String) snippet.get("title");
                    }
                    log.info("Successfully created YouTube playlist '{}' (ID: {})", playlistName, id);
                    return com.justice.playswap.ytmusic.model.CreatePlaylistResponse.builder()
                            .playlistId(id)
                            .name(playlistName)
                            .playlistUrl("https://music.youtube.com/playlist?list=" + id)
                            .build();
                })
                .doOnError(e -> log.error("Failed to create YouTube playlist '{}': {}", name, e.getMessage()));
    }

    public Mono<Boolean> addTracksToPlaylist(String playlistId, List<String> videoIds, String accessToken) {
        if (videoIds == null || videoIds.isEmpty()) {
            return Mono.just(true);
        }

        List<String> validIds = videoIds.stream()
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.toList());

        if (validIds.isEmpty()) {
            return Mono.just(true);
        }

        int estimatedQuotaCost = validIds.size() * 50;
        if (validIds.size() >= 50) {
            log.warn("⚠️ [YouTube API Quota Safeguard] Inserting {} tracks into YouTube playlist {} will consume ~{} quota units out of the 10,000 daily limit.",
                    validIds.size(), playlistId, estimatedQuotaCost);
        } else {
            log.info("==> [YoutubeApiClient] Inserting {} tracks into YouTube playlist {} (Estimated quota cost: {} units)", validIds.size(), playlistId, estimatedQuotaCost);
        }

        return reactor.core.publisher.Flux.fromIterable(validIds)
                .concatMap(videoId -> {
                    Map<String, Object> body = Map.of(
                            "snippet", Map.of(
                                    "playlistId", playlistId,
                                    "resourceId", Map.of(
                                            "kind", "youtube#video",
                                            "videoId", videoId
                                    )
                            )
                    );

                    return webClientBuilder.build()
                            .post()
                            .uri(YOUTUBE_API_BASE + "/playlistItems?part=snippet")
                            .header("Authorization", "Bearer " + accessToken)
                            .header("Content-Type", "application/json")
                            .bodyValue(body)
                            .retrieve()
                            .bodyToMono(Map.class)
                            .doOnNext(res -> log.debug("Added video {} to YouTube playlist {}", videoId, playlistId))
                            .onErrorResume(e -> {
                                log.warn("Failed to add video {} to YouTube playlist {}: {}", videoId, playlistId, e.getMessage());
                                return Mono.empty();
                            });
                })
                .then(Mono.just(true))
                .doOnError(e -> log.error("Failed to insert tracks into YouTube playlist {}: {}", playlistId, e.getMessage()))
                .onErrorReturn(false);
    }

    public Mono<List<com.justice.playswap.ytmusic.model.YoutubeTrack>> getPlaylistTracks(String playlistId, String accessToken) {
        String url = YOUTUBE_API_BASE + "/playlistItems?part=snippet,contentDetails&playlistId=" + playlistId + "&maxResults=50";
        log.info("==> [YoutubeApiClient] Fetching tracks for YouTube playlist {}", playlistId);
        return fetchPlaylistItemsRecursive(url, accessToken);
    }

    @SuppressWarnings("unchecked")
    private Mono<List<com.justice.playswap.ytmusic.model.YoutubeTrack>> fetchPlaylistItemsRecursive(String url, String accessToken) {
        if (url == null || url.isEmpty()) {
            return Mono.just(List.of());
        }

        return webClientBuilder.build()
                .get()
                .uri(url)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(response -> {
                    if (response == null || response.get("items") == null) {
                        return Mono.just(List.<com.justice.playswap.ytmusic.model.YoutubeTrack>of());
                    }

                    List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
                    List<com.justice.playswap.ytmusic.model.YoutubeTrack> tracks = items.stream()
                            .filter(java.util.Objects::nonNull)
                            .map(this::normalizeYoutubeTrack)
                            .filter(java.util.Objects::nonNull)
                            .collect(Collectors.toList());

                    String nextPageToken = (String) response.get("nextPageToken");
                    if (nextPageToken != null && !nextPageToken.isEmpty()) {
                        String baseUrl = url.replaceAll("&pageToken=[^&]*", "");
                        String nextUrl = baseUrl + "&pageToken=" + nextPageToken;
                        return fetchPlaylistItemsRecursive(nextUrl, accessToken)
                                .map(nextTracks -> {
                                    tracks.addAll(nextTracks);
                                    return tracks;
                                });
                    }

                    return Mono.just(tracks);
                })
                .doOnError(e -> log.error("Failed to fetch YouTube playlist items from {}: {}", url, e.getMessage()));
    }

    @SuppressWarnings("unchecked")
    private com.justice.playswap.ytmusic.model.YoutubeTrack normalizeYoutubeTrack(Map<String, Object> item) {
        Map<String, Object> snippet = (Map<String, Object>) item.get("snippet");
        if (snippet == null) return null;

        String title = (String) snippet.get("title");
        if (title == null || title.equals("Private video") || title.equals("Deleted video")) return null;

        String videoId = null;
        Map<String, Object> resourceId = (Map<String, Object>) snippet.get("resourceId");
        if (resourceId != null) {
            videoId = (String) resourceId.get("videoId");
        }
        if (videoId == null) {
            Map<String, Object> contentDetails = (Map<String, Object>) item.get("contentDetails");
            if (contentDetails != null) {
                videoId = (String) contentDetails.get("videoId");
            }
        }
        if (videoId == null) return null;

        String artist = (String) snippet.get("videoOwnerChannelTitle");
        if (artist == null || artist.isEmpty()) {
            artist = (String) snippet.get("channelTitle");
        }
        if (artist != null && artist.endsWith(" - Topic")) {
            artist = artist.substring(0, artist.length() - 8);
        }

        return com.justice.playswap.ytmusic.model.YoutubeTrack.builder()
                .trackId(videoId)
                .title(title)
                .artists(artist != null ? artist : "")
                .primaryArtist(artist != null ? artist : "")
                .build();
    }

    private int parseTotalTracks(Map<String, Object> item) {
        if (item == null) return 0;
        Map<String, Object> contentDetails = (Map<String, Object>) item.get("contentDetails");
        if (contentDetails != null) {
            for (String key : List.of("itemCount", "item_count", "totalItems", "total_items", "total")) {
                int count = parseNumber(contentDetails.get(key));
                if (count > 0) return count;
            }
        }
        return parseNumber(item.get("itemCount"));
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
}
