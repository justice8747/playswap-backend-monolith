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
import reactor.core.publisher.Mono;

import java.util.List;
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
        return tokenProvider.getValidAccessToken(userId, "youtube")
                .flatMap(accessToken -> youtubeApiClient.getPlaylistTracks(playlistId, accessToken))
                .map(tracks -> tracks.stream().map(t -> SpotifyTrackDto.builder()
                        .trackId(t.getTrackId())
                        .title(t.getTitle())
                        .artists(t.getArtists())
                        .primaryArtist(t.getPrimaryArtist())
                        .build()
                ).collect(Collectors.toList()))
                .onErrorReturn(List.of());
    }

    @Override
    public Mono<TrackAnalysisResultDto> analyzeTrack(String userId, SpotifyTrackDto track) {
        log.info("==> [YtmusicService] In-process analyze track '{}' for user '{}'", track.getTitle(), userId);
        TrackAnalysisRequest request = TrackAnalysisRequest.builder()
                .spotifyTrackId(track.getTrackId())
                .title(track.getTitle())
                .artist(track.getArtists())
                .primaryArtist(track.getPrimaryArtist())
                .build();

        return tokenProvider.getValidAccessToken(userId, "youtube")
                .flatMap(accessToken -> trackMatchingService.analyzeTrack(request, accessToken))
                .onErrorResume(err -> {
                    // Try without accessToken if token retrieval fails (InnerTube tiers 2 & 3 work without tokens)
                    log.warn("Attempting unauthenticated InnerTube search for '{}': {}", track.getTitle(), err.getMessage());
                    return trackMatchingService.analyzeTrack(request, null);
                })
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
        return tokenProvider.getValidAccessToken(userId, "youtube")
                .flatMap(accessToken -> youtubeApiClient.createPlaylist(name, description, accessToken))
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
        return tokenProvider.getValidAccessToken(userId, "youtube")
                .flatMap(accessToken -> youtubeApiClient.addTracksToPlaylist(playlistId, videoIds, accessToken));
    }
}
