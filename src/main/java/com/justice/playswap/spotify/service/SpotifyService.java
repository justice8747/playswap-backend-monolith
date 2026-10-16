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
import reactor.core.publisher.Mono;

import java.util.List;
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
        return tokenProvider.getValidAccessToken(userId, "spotify")
                .flatMap(accessToken -> spotifyApiClient.getPlaylistTracks(playlistId, accessToken))
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
                .build();

        return tokenProvider.getValidAccessToken(userId, "spotify")
                .flatMap(accessToken -> trackMatchingService.analyzeTrack(request, accessToken))
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
        return tokenProvider.getValidAccessToken(userId, "spotify")
                .flatMap(accessToken -> spotifyApiClient.createPlaylist(name, description, accessToken))
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
        return tokenProvider.getValidAccessToken(userId, "spotify")
                .flatMap(accessToken -> spotifyApiClient.addTracksToPlaylist(playlistId, trackIds, accessToken));
    }
}
