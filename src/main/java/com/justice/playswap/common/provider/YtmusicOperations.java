package com.justice.playswap.common.provider;

import com.justice.playswap.transfer.dto.CreatePlaylistResponseDto;
import com.justice.playswap.transfer.dto.SpotifyTrackDto;
import com.justice.playswap.transfer.dto.TrackAnalysisResultDto;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Set;

public interface YtmusicOperations {
    Mono<List<SpotifyTrackDto>> getPlaylistTracks(String userId, String playlistId);
    Mono<TrackAnalysisResultDto> analyzeTrack(String userId, SpotifyTrackDto track);
    Mono<CreatePlaylistResponseDto> createPlaylist(String userId, String name, String description);
    Mono<Boolean> addTracksToPlaylist(String userId, String playlistId, List<String> videoIds);
    Mono<Set<String>> addTracksToPlaylistWithResults(String userId, String playlistId, List<String> videoIds);
    Mono<Boolean> deletePlaylist(String userId, String playlistId);
}
