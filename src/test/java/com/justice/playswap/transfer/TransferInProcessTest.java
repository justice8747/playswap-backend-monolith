package com.justice.playswap.transfer;

import com.justice.playswap.common.provider.SpotifyOperations;
import com.justice.playswap.common.provider.YtmusicOperations;
import com.justice.playswap.transfer.dto.*;
import com.justice.playswap.transfer.model.*;
import com.justice.playswap.transfer.repository.*;
import com.justice.playswap.transfer.service.PlaylistAnalysisService;
import com.justice.playswap.transfer.service.PlaylistTransferExecutionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransferInProcessTest {

    @Mock
    private TransferSessionRepository sessionRepository;
    @Mock
    private TransferJobRepository jobRepository;
    @Mock
    private TransferItemRepository itemRepository;
    @Mock
    private SongMappingRepository songMappingRepository;
    @Mock
    private TransferHistoryRepository transferHistoryRepository;
    @Mock
    private SpotifyOperations spotifyOperations;
    @Mock
    private YtmusicOperations ytmusicOperations;

    private PlaylistAnalysisService analysisService;
    private PlaylistTransferExecutionService executionService;

    @BeforeEach
    void setUp() {
        analysisService = new PlaylistAnalysisService(
                sessionRepository,
                jobRepository,
                itemRepository,
                songMappingRepository,
                spotifyOperations,
                ytmusicOperations
        );

        executionService = new PlaylistTransferExecutionService(
                sessionRepository,
                jobRepository,
                itemRepository,
                transferHistoryRepository,
                spotifyOperations,
                ytmusicOperations
        );
    }

    @Test
    void testAnalysisCallsInProcessSpotifyOperationsDirectly() {
        UUID sessionId = UUID.randomUUID();
        TransferSession session = TransferSession.builder()
                .id(sessionId)
                .status("ANALYZING")
                .build();

        TransferJob job = TransferJob.builder()
                .id(UUID.randomUUID())
                .sessionId(sessionId)
                .sourcePlaylistId("playlist-1")
                .status("ANALYZING")
                .build();

        when(sessionRepository.save(any(TransferSession.class))).thenReturn(session);
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(jobRepository.save(any(TransferJob.class))).thenReturn(job);

        // In-process Spotify tracks call
        SpotifyTrackDto trackDto = SpotifyTrackDto.builder()
                .trackId("track-123")
                .title("Blinding Lights")
                .artists("The Weeknd")
                .primaryArtist("The Weeknd")
                .build();
        when(spotifyOperations.getPlaylistTracks(eq("user-1"), eq("playlist-1")))
                .thenReturn(Mono.just(List.of(trackDto)));

        // In-process YouTube analysis call
        TrackAnalysisResultDto analysisResult = TrackAnalysisResultDto.builder()
                .spotifyTrackId("track-123")
                .youtubeVideoId("yt-video-456")
                .matched(true)
                .confidence(95)
                .build();
        when(ytmusicOperations.analyzeTrack(eq("user-1"), any(SpotifyTrackDto.class)))
                .thenReturn(Mono.just(analysisResult));

        when(songMappingRepository.findBySourcePlatformAndSourceTrackIdAndDestinationPlatform(anyString(), anyString(), anyString()))
                .thenReturn(Optional.empty());

        AnalyzePlaylistsRequest request = AnalyzePlaylistsRequest.builder()
                .playlistIds(List.of("playlist-1"))
                .sourcePlatform("SPOTIFY")
                .targetPlatform("YTMUSIC")
                .build();

        StepVerifier.create(analysisService.analyzePlaylists("user-1", request))
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals(1, response.getTotalPlaylists());
                    assertEquals(1, response.getTotalTracks());
                    assertEquals(1, response.getMatchedTracks());
                })
                .verifyComplete();

        // Verify direct in-process method invocation without HTTP
        verify(spotifyOperations, times(1)).getPlaylistTracks("user-1", "playlist-1");
        verify(ytmusicOperations, times(1)).analyzeTrack(eq("user-1"), any(SpotifyTrackDto.class));
    }

    @Test
    void testExecutionCallsInProcessYtmusicOperationsDirectly() {
        UUID sessionId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();

        TransferSession session = TransferSession.builder()
                .id(sessionId)
                .sourcePlatform("SPOTIFY")
                .targetPlatform("YTMUSIC")
                .status("READY")
                .build();

        TransferJob job = TransferJob.builder()
                .id(jobId)
                .sessionId(sessionId)
                .sourcePlaylistId("playlist-1")
                .sourcePlaylistName("Top Hits")
                .status("READY")
                .build();

        TransferItem item = TransferItem.builder()
                .id(UUID.randomUUID())
                .jobId(jobId)
                .sourceTrackId("sp-1")
                .targetTrackId("yt-1")
                .status(ItemStatus.PENDING.name())
                .build();

        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(jobRepository.findBySessionId(sessionId)).thenReturn(List.of(job));
        when(itemRepository.findByJobId(jobId)).thenReturn(List.of(item));

        // In-process playlist creation call
        CreatePlaylistResponseDto createdDto = CreatePlaylistResponseDto.builder()
                .playlistId("yt-playlist-99")
                .name("Top Hits (PlaySwap)")
                .playlistUrl("https://music.youtube.com/playlist?list=yt-playlist-99")
                .build();
        when(ytmusicOperations.createPlaylist(eq("user-1"), anyString(), anyString()))
                .thenReturn(Mono.just(createdDto));

        // In-process add tracks call
        when(ytmusicOperations.addTracksToPlaylist(eq("user-1"), eq("yt-playlist-99"), eq(List.of("yt-1"))))
                .thenReturn(Mono.just(true));

        ExecuteTransferRequest request = ExecuteTransferRequest.builder()
                .sessionId(sessionId)
                .build();

        StepVerifier.create(executionService.executeTransfer("user-1", request))
                .assertNext(response -> {
                    assertNotNull(response);
                    assertEquals(TransferStatus.COMPLETED.name(), response.getStatus());
                    assertEquals(1, response.getTotalPlaylists());
                    assertEquals(1, response.getTotalTransferredTracks());
                })
                .verifyComplete();

        // Verify direct in-process method invocation without HTTP
        verify(ytmusicOperations, times(1)).createPlaylist(eq("user-1"), anyString(), anyString());
        verify(ytmusicOperations, times(1)).addTracksToPlaylist("user-1", "yt-playlist-99", List.of("yt-1"));
    }
}
