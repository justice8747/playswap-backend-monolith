package com.justice.playswap.spotify.controller;

import com.justice.playswap.common.provider.TokenProvider;
import com.justice.playswap.spotify.client.SpotifyApiClient;
import com.justice.playswap.spotify.model.*;
import com.justice.playswap.spotify.service.PlaylistCacheService;
import com.justice.playswap.spotify.service.TrackMatchingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/spotify")
@RequiredArgsConstructor
public class SpotifyController {

    private final TokenProvider tokenProvider;
    private final SpotifyApiClient spotifyApiClient;
    private final PlaylistCacheService playlistCacheService;
    private final TrackMatchingService trackMatchingService;

    @GetMapping("/playlists")
    public Mono<List<SpotifyPlaylist>> getPlaylists(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @RequestParam(value = "refresh", defaultValue = "false") boolean forceRefresh) {

        String userId = resolveUserId(jwt, headerUserId);
        if (userId == null || userId.isBlank()) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authenticated user"));
        }

        if (forceRefresh) {
            log.info("Force refresh requested for user {} (Spotify playlists)", userId);
            playlistCacheService.evict(userId);
        } else {
            List<SpotifyPlaylist> cachedPlaylists = playlistCacheService.get(userId);
            if (cachedPlaylists != null) {
                return Mono.just(cachedPlaylists);
            }
        }

        return tokenProvider.getValidAccessToken(userId, "spotify")
                .flatMap(accessToken -> spotifyApiClient.getUserPlaylists(accessToken))
                .doOnNext(playlists -> playlistCacheService.put(userId, playlists));
    }

    @GetMapping("/playlists/{playlistId}/tracks")
    public Mono<List<SpotifyTrack>> getPlaylistTracks(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @PathVariable("playlistId") String playlistId) {

        String userId = resolveUserId(jwt, headerUserId);
        if (userId == null || userId.isBlank()) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authenticated user"));
        }

        log.info("==> [SpotifyController] Request received for playlist: '{}' | User: '{}'", playlistId, userId);
        return tokenProvider.getValidAccessToken(userId, "spotify")
                .flatMap(accessToken -> spotifyApiClient.getPlaylistTracks(playlistId, accessToken));
    }

    @PostMapping("/analyze-track")
    public Mono<TrackAnalysisResult> analyzeTrack(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @RequestBody TrackAnalysisRequest request) {

        String userId = resolveUserId(jwt, headerUserId);
        if (userId == null || userId.isBlank()) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authenticated user"));
        }

        return tokenProvider.getValidAccessToken(userId, "spotify")
                .flatMap(accessToken -> trackMatchingService.analyzeTrack(request, accessToken));
    }

    @PostMapping("/playlists/create")
    public Mono<CreatePlaylistResponse> createPlaylist(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @RequestBody CreatePlaylistRequest request) {

        String userId = resolveUserId(jwt, headerUserId);
        if (userId == null || userId.isBlank()) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authenticated user"));
        }

        log.info("==> [SpotifyController] Creating Spotify playlist '{}' for user '{}'", request.getName(), userId);
        return tokenProvider.getValidAccessToken(userId, "spotify")
                .flatMap(accessToken -> spotifyApiClient.createPlaylist(request.getName(), request.getDescription(), accessToken));
    }

    @PostMapping("/playlists/{playlistId}/add-tracks")
    public Mono<Boolean> addTracksToPlaylist(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @PathVariable("playlistId") String playlistId,
            @RequestBody AddTracksRequest request) {

        String userId = resolveUserId(jwt, headerUserId);
        if (userId == null || userId.isBlank()) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authenticated user"));
        }

        log.info("==> [SpotifyController] Adding {} tracks to Spotify playlist '{}' for user '{}'",
                request.getTrackIds() != null ? request.getTrackIds().size() : 0, playlistId, userId);
        return tokenProvider.getValidAccessToken(userId, "spotify")
                .flatMap(accessToken -> spotifyApiClient.addTracksToPlaylist(playlistId, request.getTrackIds(), accessToken));
    }

    private String resolveUserId(Jwt jwt, String headerUserId) {
        if (jwt != null) {
            String preferredUsername = jwt.getClaimAsString("preferred_username");
            if (preferredUsername != null && !preferredUsername.isBlank()) {
                return preferredUsername;
            }
            if (jwt.getSubject() != null && !jwt.getSubject().isBlank()) {
                return jwt.getSubject();
            }
        }
        return headerUserId;
    }
}
