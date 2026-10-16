package com.justice.playswap.ytmusic.controller;

import com.justice.playswap.common.provider.TokenProvider;
import com.justice.playswap.ytmusic.client.YoutubeApiClient;
import com.justice.playswap.ytmusic.model.*;
import com.justice.playswap.ytmusic.service.PlaylistCacheService;
import com.justice.playswap.ytmusic.service.TrackMatchingService;
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
@RequestMapping("/api/ytmusic")
@RequiredArgsConstructor
public class YtmusicController {

    private final TokenProvider tokenProvider;
    private final YoutubeApiClient youtubeApiClient;
    private final PlaylistCacheService playlistCacheService;
    private final TrackMatchingService trackMatchingService;

    @GetMapping("/playlists")
    public Mono<List<YoutubePlaylist>> getPlaylists(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @RequestParam(value = "refresh", defaultValue = "false") boolean forceRefresh) {

        String userId = resolveUserId(jwt, headerUserId);
        if (userId == null || userId.isBlank()) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authenticated user"));
        }

        if (forceRefresh) {
            log.info("Force refresh requested for user {} (YouTube playlists)", userId);
            playlistCacheService.evict(userId);
        } else {
            List<YoutubePlaylist> cachedPlaylists = playlistCacheService.get(userId);
            if (cachedPlaylists != null) {
                return Mono.just(cachedPlaylists);
            }
        }

        return tokenProvider.getValidAccessToken(userId, "youtube")
                .flatMap(accessToken -> youtubeApiClient.getUserPlaylists(accessToken))
                .doOnNext(playlists -> playlistCacheService.put(userId, playlists));
    }

    @GetMapping("/playlists/{playlistId}/tracks")
    public Mono<List<YoutubeTrack>> getPlaylistTracks(
            @AuthenticationPrincipal Jwt jwt,
            @RequestHeader(value = "X-User-Id", required = false) String headerUserId,
            @PathVariable("playlistId") String playlistId) {

        String userId = resolveUserId(jwt, headerUserId);
        if (userId == null || userId.isBlank()) {
            return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing authenticated user"));
        }

        log.info("==> [YtmusicController] Request received for YouTube playlist tracks: '{}' | User: '{}'", playlistId, userId);
        return tokenProvider.getValidAccessToken(userId, "youtube")
                .flatMap(accessToken -> youtubeApiClient.getPlaylistTracks(playlistId, accessToken));
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

        return tokenProvider.getValidAccessToken(userId, "youtube")
                .flatMap(accessToken -> trackMatchingService.analyzeTrack(request, accessToken))
                .onErrorResume(err -> trackMatchingService.analyzeTrack(request, null));
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

        log.info("==> [YtmusicController] Creating YouTube playlist '{}' for user '{}'", request.getName(), userId);
        return tokenProvider.getValidAccessToken(userId, "youtube")
                .flatMap(accessToken -> youtubeApiClient.createPlaylist(request.getName(), request.getDescription(), accessToken));
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

        log.info("==> [YtmusicController] Adding {} tracks to YouTube playlist '{}' for user '{}'",
                request.getVideoIds() != null ? request.getVideoIds().size() : 0, playlistId, userId);
        return tokenProvider.getValidAccessToken(userId, "youtube")
                .flatMap(accessToken -> youtubeApiClient.addTracksToPlaylist(playlistId, request.getVideoIds(), accessToken));
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
