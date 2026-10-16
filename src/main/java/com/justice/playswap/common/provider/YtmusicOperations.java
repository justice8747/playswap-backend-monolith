package com.justice.playswap.common.provider;

import com.justice.playswap.transfer.dto.CreatePlaylistResponseDto;
import com.justice.playswap.transfer.dto.SpotifyTrackDto;
import com.justice.playswap.transfer.dto.TrackAnalysisResultDto;
import reactor.core.publisher.Mono;

import java.util.List;

public interface YtmusicOperations {
    Mono<List<SpotifyTrackDto>> getPlaylistTracks(String userId, String playlistId);
    Mono<TrackAnalysisResultDto> analyzeTrack(String userId, SpotifyTrackDto track);
    Mono<CreatePlaylistResponseDto> createPlaylist(String userId, String name, String description);
    Mono<Boolean> addTracksToPlaylist(String userId, String playlistId, List<String> videoIds);
}
