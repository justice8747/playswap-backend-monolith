package com.justice.playswap.ytmusic.service;

import com.justice.playswap.common.provider.TokenProvider;
import com.justice.playswap.common.provider.YtmusicOperations;
import com.justice.playswap.transfer.dto.CreatePlaylistResponseDto;
import com.justice.playswap.transfer.dto.SpotifyTrackDto;
import com.justice.playswap.transfer.dto.TrackAnalysisResultDto;
import com.justice.playswap.ytmusic.client.YoutubeApiClient;
import com.justice.playswap.ytmusic.model.TrackAnalysisRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class YtmusicService implements YtmusicOperations {

    private final TokenProvider tokenProvider;
    private final YoutubeApiClient youtubeApiClient;
    private final TrackMatchingService trackMatchingService;

    @Override
    public Mono<List<SpotifyTrackDto>> getPlaylistTracks(String userId, String playlistId) {
        log.info("==> [YtmusicService] Fetching tracks in-process for playlist '{}' and user '{}'", playlistId, userId);
        return tokenProvider.withValidAccessToken(userId, "youtube",
                        accessToken -> youtubeApiClient.getPlaylistTracks(playlistId, accessToken))
                .map(tracks -> tracks.stream().map(t -> SpotifyTrackDto.builder()
                        .trackId(t.getTrackId())
                        .title(t.getTitle())
                        .artists(t.getArtists())
                        .primaryArtist(t.getPrimaryArtist())
                        .build()
                ).collect(Collectors.toList()))
                .onErrorResume(error -> {
                    if (error instanceof org.springframework.web.reactive.function.client.WebClientResponseException responseError
                            && responseError.getStatusCode().value() == 401) {
                        return Mono.error(error);
                    }
                    return Mono.just(List.of());
                });
    }

    @Override
    public Mono<TrackAnalysisResultDto> analyzeTrack(String userId, SpotifyTrackDto track) {
        log.info("==> [YtmusicService] In-process analyze track '{}' for user '{}'", track.getTitle(), userId);
        TrackAnalysisRequest request = TrackAnalysisRequest.builder()
                .spotifyTrackId(track.getTrackId())
                .title(track.getTitle())
                .artist(track.getArtists())
                .primaryArtist(track.getPrimaryArtist())
                .album(track.getAlbum())
                .durationMs(track.getDurationMs())
                .isrc(track.getIsrc())
                .build();

        return tokenProvider.getValidAccessToken(userId, "youtube")
                .thenReturn(true)
                .doOnError(error -> log.warn("Could not load YouTube token for '{}'; using InnerTube only: {}",
                        track.getTitle(), error.getMessage()))
                .onErrorReturn(false)
                .defaultIfEmpty(false)
                .flatMap(hasToken -> hasToken
                        ? tokenProvider.withValidAccessToken(userId, "youtube",
                                accessToken -> trackMatchingService.analyzeTrack(request, accessToken))
                        : trackMatchingService.analyzeTrack(request, null))
                .map(res -> TrackAnalysisResultDto.builder()
                        .spotifyTrackId(res.getSpotifyTrackId())
                        .title(res.getTitle())
                        .artist(res.getArtist())
                        .matched(res.isMatched())
                        .youtubeVideoId(res.getYoutubeVideoId())
                        .confidence(res.getConfidence())
                        .failureReason(res.getFailureReason())
                        .build());
    }

    @Override
    public Mono<CreatePlaylistResponseDto> createPlaylist(String userId, String name, String description) {
        log.info("==> [YtmusicService] In-process create playlist '{}' for user '{}'", name, userId);
        return tokenProvider.withValidAccessTokenWithoutRetry(userId, "youtube",
                        accessToken -> youtubeApiClient.createPlaylist(name, description, accessToken))
                .map(res -> CreatePlaylistResponseDto.builder()
                        .playlistId(res.getPlaylistId())
                        .name(res.getName())
                        .playlistUrl(res.getPlaylistUrl())
                        .build());
    }

    @Override
    public Mono<Boolean> addTracksToPlaylist(String userId, String playlistId, List<String> videoIds) {
        log.info("==> [YtmusicService] In-process adding {} tracks to playlist '{}' for user '{}'",
                videoIds != null ? videoIds.size() : 0, playlistId, userId);
        long expectedTrackCount = videoIds == null ? 0 : videoIds.stream()
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .count();
        return addTracksToPlaylistWithResults(userId, playlistId, videoIds)
                .map(insertedIds -> insertedIds.size() == expectedTrackCount);
    }

    @Override
    public Mono<Set<String>> addTracksToPlaylistWithResults(String userId, String playlistId, List<String> videoIds) {
        return Flux.fromIterable(videoIds == null ? List.<String>of() : videoIds)
                .filter(id -> id != null && !id.isBlank())
                .concatMap(videoId -> tokenProvider.withValidAccessToken(userId, "youtube",
                                accessToken -> youtubeApiClient.addTracksToPlaylistWithResults(
                                        playlistId, List.of(videoId), accessToken))
                        .doOnError(error -> log.warn("Failed to add YouTube video {} to playlist {}: {}",
                                videoId, playlistId, error.getMessage()))
                        .onErrorResume(error -> Mono.just(Set.of())))
                .flatMapIterable(ids -> ids)
                .collect(Collectors.toSet());
    }

    @Override
    public Mono<Boolean> deletePlaylist(String userId, String playlistId) {
        return tokenProvider.withValidAccessToken(userId, "youtube",
                accessToken -> youtubeApiClient.deletePlaylist(playlistId, accessToken));
    }
}
