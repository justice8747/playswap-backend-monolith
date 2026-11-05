package com.justice.playswap.spotify.service;

import com.justice.playswap.common.provider.SpotifyOperations;
import com.justice.playswap.common.provider.TokenProvider;
import com.justice.playswap.spotify.client.SpotifyApiClient;
import com.justice.playswap.spotify.model.TrackAnalysisRequest;
import com.justice.playswap.transfer.dto.CreatePlaylistResponseDto;
import com.justice.playswap.transfer.dto.SpotifyTrackDto;
import com.justice.playswap.transfer.dto.TrackAnalysisResultDto;
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
public class SpotifyService implements SpotifyOperations {

    private final TokenProvider tokenProvider;
    private final SpotifyApiClient spotifyApiClient;
    private final TrackMatchingService trackMatchingService;

    @Override
    public Mono<List<SpotifyTrackDto>> getPlaylistTracks(String userId, String playlistId) {
        log.info("==> [SpotifyService] Fetching tracks in-process for playlist '{}' and user '{}'", playlistId, userId);
        return tokenProvider.withValidAccessToken(userId, "spotify",
                        accessToken -> spotifyApiClient.getPlaylistTracks(playlistId, accessToken))
                .map(tracks -> tracks.stream().map(t -> SpotifyTrackDto.builder()
                        .trackId(t.getTrackId())
                        .title(t.getTitle())
                        .artists(t.getArtists())
                        .primaryArtist(t.getPrimaryArtist())
                        .album(t.getAlbum())
                        .durationMs(t.getDurationMs())
                        .isrc(t.getIsrc())
                        .build()
                ).collect(Collectors.toList()));
    }

    @Override
    public Mono<TrackAnalysisResultDto> analyzeTrack(String userId, SpotifyTrackDto track) {
        log.info("==> [SpotifyService] In-process analyze track '{}' for user '{}'", track.getTitle(), userId);
        TrackAnalysisRequest request = TrackAnalysisRequest.builder()
                .title(track.getTitle())
                .artist(track.getArtists())
                .primaryArtist(track.getPrimaryArtist())
                .album(track.getAlbum())
                .durationMs(track.getDurationMs())
                .isrc(track.getIsrc())
                .build();

        return tokenProvider.withValidAccessToken(userId, "spotify",
                        accessToken -> trackMatchingService.analyzeTrack(request, accessToken))
                .map(res -> TrackAnalysisResultDto.builder()
                        .spotifyTrackId(res.getSpotifyTrackId())
                        .title(res.getTitle())
                        .artist(res.getArtist())
                        .matched(res.isMatched())
                        .confidence(res.getConfidence())
                        .failureReason(res.getFailureReason())
                        .build());
    }

    @Override
    public Mono<CreatePlaylistResponseDto> createPlaylist(String userId, String name, String description) {
        log.info("==> [SpotifyService] In-process create playlist '{}' for user '{}'", name, userId);
        return tokenProvider.withValidAccessTokenWithoutRetry(userId, "spotify",
                        accessToken -> spotifyApiClient.createPlaylist(name, description, accessToken))
                .map(res -> CreatePlaylistResponseDto.builder()
                        .playlistId(res.getPlaylistId())
                        .name(res.getName())
                        .playlistUrl(res.getPlaylistUrl())
                        .build());
    }

    @Override
    public Mono<Boolean> addTracksToPlaylist(String userId, String playlistId, List<String> trackIds) {
        log.info("==> [SpotifyService] In-process adding {} tracks to playlist '{}' for user '{}'",
                trackIds != null ? trackIds.size() : 0, playlistId, userId);
        long expectedTrackCount = trackIds == null ? 0 : trackIds.stream()
                .filter(id -> id != null && !id.isBlank())
                .distinct()
                .count();
        return addTracksToPlaylistWithResults(userId, playlistId, trackIds)
                .map(insertedIds -> insertedIds.size() == expectedTrackCount);
    }

    @Override
    public Mono<Set<String>> addTracksToPlaylistWithResults(String userId, String playlistId, List<String> trackIds) {
        return Flux.fromIterable(trackIds == null ? List.<String>of() : trackIds)
                .filter(id -> id != null && !id.isBlank())
                .buffer(100)
                .concatMap(batch -> tokenProvider.withValidAccessToken(userId, "spotify",
                                accessToken -> spotifyApiClient.addTracksToPlaylistWithResults(
                                        playlistId, batch, accessToken))
                        .doOnError(error -> log.warn("Failed to add a Spotify batch of {} tracks to playlist {}: {}",
                                batch.size(), playlistId, error.getMessage()))
                        .onErrorResume(error -> Mono.just(Set.of())))
                .flatMapIterable(ids -> ids)
                .collect(Collectors.toSet());
    }

    @Override
    public Mono<Boolean> deletePlaylist(String userId, String playlistId) {
        return tokenProvider.withValidAccessToken(userId, "spotify",
                accessToken -> spotifyApiClient.deletePlaylist(playlistId, accessToken));
    }
}
