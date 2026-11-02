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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Optional;
import java.util.Set;
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
                .youtubeVideoId("Abc123_XyZ0")
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
                    assertEquals(0, response.getCachedMatches());
                })
                .verifyComplete();

        // Verify direct in-process method invocation without HTTP
        verify(spotifyOperations, times(1)).getPlaylistTracks("user-1", "playlist-1");
        verify(ytmusicOperations, times(1)).analyzeTrack(eq("user-1"), any(SpotifyTrackDto.class));
        ArgumentCaptor<SongMapping> mappingCaptor = ArgumentCaptor.forClass(SongMapping.class);
        verify(songMappingRepository).save(mappingCaptor.capture());
        assertEquals("Abc123_XyZ0", mappingCaptor.getValue().getDestinationTrackId());
        ArgumentCaptor<TransferItem> itemCaptor = ArgumentCaptor.forClass(TransferItem.class);
        verify(itemRepository).save(itemCaptor.capture());
        assertEquals("Abc123_XyZ0", itemCaptor.getValue().getTargetTrackId());
    }

    @Test
    void persistsProviderAnalysisErrorsAsFailedTrackItems() {
        UUID sessionId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        TransferSession session = TransferSession.builder().id(sessionId).status("ANALYZING").build();
        TransferJob job = TransferJob.builder()
                .id(jobId)
                .sessionId(sessionId)
                .sourcePlaylistId("playlist-1")
                .status("ANALYZING")
                .build();
        SpotifyTrackDto track = SpotifyTrackDto.builder()
                .trackId("source-track")
                .title("Track Title")
                .artists("Artist")
                .build();

        when(sessionRepository.save(any(TransferSession.class))).thenReturn(session);
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(jobRepository.save(any(TransferJob.class))).thenReturn(job);
        when(spotifyOperations.getPlaylistTracks("user-1", "playlist-1"))
                .thenReturn(Mono.just(List.of(track)));
        when(songMappingRepository.findBySourcePlatformAndSourceTrackIdAndDestinationPlatform(
                "SPOTIFY", "source-track", "YTMUSIC"))
                .thenReturn(Optional.empty());
        when(ytmusicOperations.analyzeTrack("user-1", track))
                .thenReturn(Mono.error(new IllegalStateException("InnerTube unavailable")));

        AnalyzePlaylistsRequest request = AnalyzePlaylistsRequest.builder()
                .playlistIds(List.of("playlist-1"))
                .sourcePlatform("SPOTIFY")
                .targetPlatform("YTMUSIC")
                .build();

        StepVerifier.create(analysisService.analyzePlaylists("user-1", request))
                .assertNext(response -> {
                    assertEquals(1, response.getTotalTracks());
                    assertEquals(0, response.getMatchedTracks());
                    assertEquals(1, response.getUnmatchedTracks());
                })
                .verifyComplete();

        ArgumentCaptor<TransferItem> itemCaptor = ArgumentCaptor.forClass(TransferItem.class);
        verify(itemRepository).save(itemCaptor.capture());
        TransferItem item = itemCaptor.getValue();
        assertEquals(ItemStatus.FAILED.name(), item.getStatus());
        assertEquals("Analysis provider error: InnerTube unavailable", item.getErrorMessage());
        assertEquals("source-track", item.getSourceTrackId());
        assertNull(item.getTargetTrackId());
    }

    @Test
    void preservesNoMatchReasonAsSkippedItemOutcome() {
        UUID sessionId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        TransferSession session = TransferSession.builder().id(sessionId).status("ANALYZING").build();
        TransferJob job = TransferJob.builder()
                .id(jobId)
                .sessionId(sessionId)
                .sourcePlaylistId("playlist-1")
                .status("ANALYZING")
                .build();
        SpotifyTrackDto track = SpotifyTrackDto.builder()
                .trackId("source-track")
                .title("Unmatched Title")
                .artists("Artist")
                .build();

        when(sessionRepository.save(any(TransferSession.class))).thenReturn(session);
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(jobRepository.save(any(TransferJob.class))).thenReturn(job);
        when(spotifyOperations.getPlaylistTracks("user-1", "playlist-1"))
                .thenReturn(Mono.just(List.of(track)));
        when(songMappingRepository.findBySourcePlatformAndSourceTrackIdAndDestinationPlatform(
                "SPOTIFY", "source-track", "YTMUSIC"))
                .thenReturn(Optional.empty());
        when(ytmusicOperations.analyzeTrack("user-1", track))
                .thenReturn(Mono.just(TrackAnalysisResultDto.builder()
                        .matched(false)
                        .confidence(0)
                        .failureReason("LOW_CONFIDENCE")
                        .build()));

        AnalyzePlaylistsRequest request = AnalyzePlaylistsRequest.builder()
                .playlistIds(List.of("playlist-1"))
                .sourcePlatform("SPOTIFY")
                .targetPlatform("YTMUSIC")
                .build();

        StepVerifier.create(analysisService.analyzePlaylists("user-1", request))
                .assertNext(response -> {
                    assertEquals(1, response.getTotalTracks());
                    assertEquals(1, response.getUnmatchedTracks());
                })
                .verifyComplete();

        ArgumentCaptor<TransferItem> itemCaptor = ArgumentCaptor.forClass(TransferItem.class);
        verify(itemRepository).save(itemCaptor.capture());
        assertEquals(ItemStatus.SKIPPED.name(), itemCaptor.getValue().getStatus());
        assertEquals("LOW_CONFIDENCE", itemCaptor.getValue().getErrorMessage());
    }

    @Test
    void persistsMatcherReportedAnalysisErrorAsFailedItem() {
        UUID sessionId = UUID.randomUUID();
        UUID jobId = UUID.randomUUID();
        TransferSession session = TransferSession.builder().id(sessionId).status("ANALYZING").build();
        TransferJob job = TransferJob.builder()
                .id(jobId)
                .sessionId(sessionId)
                .sourcePlaylistId("playlist-1")
                .status("ANALYZING")
                .build();
        SpotifyTrackDto track = SpotifyTrackDto.builder()
                .trackId("source-track")
                .title("Track Title")
                .artists("Artist")
                .build();

        when(sessionRepository.save(any(TransferSession.class))).thenReturn(session);
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(jobRepository.save(any(TransferJob.class))).thenReturn(job);
        when(spotifyOperations.getPlaylistTracks("user-1", "playlist-1"))
                .thenReturn(Mono.just(List.of(track)));
        when(songMappingRepository.findBySourcePlatformAndSourceTrackIdAndDestinationPlatform(
                "SPOTIFY", "source-track", "YTMUSIC"))
                .thenReturn(Optional.empty());
        when(ytmusicOperations.analyzeTrack("user-1", track))
                .thenReturn(Mono.just(TrackAnalysisResultDto.builder()
                        .matched(false)
                        .failureReason("ANALYSIS_ERROR")
                        .build()));

        AnalyzePlaylistsRequest request = AnalyzePlaylistsRequest.builder()
                .playlistIds(List.of("playlist-1"))
                .sourcePlatform("SPOTIFY")
                .targetPlatform("YTMUSIC")
                .build();

        StepVerifier.create(analysisService.analyzePlaylists("user-1", request))
                .assertNext(response -> assertEquals(1, response.getUnmatchedTracks()))
                .verifyComplete();

        ArgumentCaptor<TransferItem> itemCaptor = ArgumentCaptor.forClass(TransferItem.class);
        verify(itemRepository).save(itemCaptor.capture());
        assertEquals(ItemStatus.FAILED.name(), itemCaptor.getValue().getStatus());
        assertEquals("ANALYSIS_ERROR", itemCaptor.getValue().getErrorMessage());
    }

    @Test
    void reportsTierOneCacheHitsAndUsesTheCachedDestinationId() {
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
        SpotifyTrackDto track = SpotifyTrackDto.builder()
                .trackId("source-track")
                .title("Cached Track")
                .artists("Artist")
                .build();
        SongMapping mapping = SongMapping.builder()
                .sourcePlatform("SPOTIFY")
                .sourceTrackId("source-track")
                .destinationPlatform("YTMUSIC")
                .destinationTrackId("cachedyt123")
                .confidenceScore(90)
                .build();

        when(sessionRepository.save(any(TransferSession.class))).thenReturn(session);
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(jobRepository.save(any(TransferJob.class))).thenReturn(job);
        when(spotifyOperations.getPlaylistTracks("user-1", "playlist-1"))
                .thenReturn(Mono.just(List.of(track)));
        when(songMappingRepository.findBySourcePlatformAndSourceTrackIdAndDestinationPlatform(
                "SPOTIFY", "source-track", "YTMUSIC"))
                .thenReturn(Optional.of(mapping));

        AnalyzePlaylistsRequest request = AnalyzePlaylistsRequest.builder()
                .playlistIds(List.of("playlist-1"))
                .sourcePlatform("SPOTIFY")
                .targetPlatform("YTMUSIC")
                .build();

        StepVerifier.create(analysisService.analyzePlaylists("user-1", request))
                .assertNext(response -> {
                    assertEquals(1, response.getCachedMatches());
                    assertEquals(1, response.getMatchedTracks());
                })
                .verifyComplete();

        ArgumentCaptor<TransferItem> itemCaptor = ArgumentCaptor.forClass(TransferItem.class);
        verify(itemRepository).save(itemCaptor.capture());
        assertEquals("cachedyt123", itemCaptor.getValue().getTargetTrackId());
        verify(ytmusicOperations, never()).analyzeTrack(anyString(), any(SpotifyTrackDto.class));
    }

    @Test
    void discardsLegacySourceIdCachedAsYoutubeDestinationAndRematches() {
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
        SpotifyTrackDto track = SpotifyTrackDto.builder()
                .trackId("spotifySourceTrackId22")
                .title("Recovered Track")
                .artists("Artist")
                .build();
        SongMapping invalidMapping = SongMapping.builder()
                .sourcePlatform("SPOTIFY")
                .sourceTrackId(track.getTrackId())
                .destinationPlatform("YTMUSIC")
                .destinationTrackId(track.getTrackId())
                .confidenceScore(90)
                .build();
        TrackAnalysisResultDto analysisResult = TrackAnalysisResultDto.builder()
                .spotifyTrackId(track.getTrackId())
                .youtubeVideoId("Abc123_XyZ0")
                .matched(true)
                .confidence(95)
                .build();

        when(sessionRepository.save(any(TransferSession.class))).thenReturn(session);
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(jobRepository.save(any(TransferJob.class))).thenReturn(job);
        when(spotifyOperations.getPlaylistTracks("user-1", "playlist-1"))
                .thenReturn(Mono.just(List.of(track)));
        when(songMappingRepository.findBySourcePlatformAndSourceTrackIdAndDestinationPlatform(
                "SPOTIFY", track.getTrackId(), "YTMUSIC"))
                .thenReturn(Optional.of(invalidMapping));
        when(ytmusicOperations.analyzeTrack("user-1", track)).thenReturn(Mono.just(analysisResult));

        AnalyzePlaylistsRequest request = AnalyzePlaylistsRequest.builder()
                .playlistIds(List.of("playlist-1"))
                .sourcePlatform("SPOTIFY")
                .targetPlatform("YTMUSIC")
                .build();

        StepVerifier.create(analysisService.analyzePlaylists("user-1", request))
                .assertNext(response -> {
                    assertEquals(0, response.getCachedMatches());
                    assertEquals(1, response.getMatchedTracks());
                })
                .verifyComplete();

        verify(songMappingRepository).delete(invalidMapping);
        ArgumentCaptor<SongMapping> mappingCaptor = ArgumentCaptor.forClass(SongMapping.class);
        verify(songMappingRepository).save(mappingCaptor.capture());
        assertEquals("Abc123_XyZ0", mappingCaptor.getValue().getDestinationTrackId());
    }

    @Test
    void usesSpotifyTrackIdWhenSpotifyIsTheDestination() {
        UUID sessionId = UUID.randomUUID();
        TransferSession session = TransferSession.builder()
                .id(sessionId)
                .status("ANALYZING")
                .build();
        TransferJob job = TransferJob.builder()
                .id(UUID.randomUUID())
                .sessionId(sessionId)
                .sourcePlaylistId("youtube-playlist")
                .status("ANALYZING")
                .build();
        SpotifyTrackDto track = SpotifyTrackDto.builder()
                .trackId("source-youtube-video")
                .title("Transfer Track")
                .artists("Artist")
                .build();
        TrackAnalysisResultDto analysisResult = TrackAnalysisResultDto.builder()
                .spotifyTrackId("spotify-target-track")
                .youtubeVideoId("source-youtube-video")
                .matched(true)
                .confidence(95)
                .build();

        when(sessionRepository.save(any(TransferSession.class))).thenReturn(session);
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(jobRepository.save(any(TransferJob.class))).thenReturn(job);
        when(ytmusicOperations.getPlaylistTracks("user-1", "youtube-playlist"))
                .thenReturn(Mono.just(List.of(track)));
        when(songMappingRepository.findBySourcePlatformAndSourceTrackIdAndDestinationPlatform(
                "YTMUSIC", track.getTrackId(), "SPOTIFY"))
                .thenReturn(Optional.empty());
        when(spotifyOperations.analyzeTrack("user-1", track)).thenReturn(Mono.just(analysisResult));

        AnalyzePlaylistsRequest request = AnalyzePlaylistsRequest.builder()
                .playlistIds(List.of("youtube-playlist"))
                .sourcePlatform("YTMUSIC")
                .targetPlatform("SPOTIFY")
                .build();

        StepVerifier.create(analysisService.analyzePlaylists("user-1", request))
                .assertNext(response -> assertEquals(1, response.getMatchedTracks()))
                .verifyComplete();

        ArgumentCaptor<SongMapping> mappingCaptor = ArgumentCaptor.forClass(SongMapping.class);
        verify(songMappingRepository).save(mappingCaptor.capture());
        assertEquals("spotify-target-track", mappingCaptor.getValue().getDestinationTrackId());
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
        when(ytmusicOperations.addTracksToPlaylistWithResults(
                eq("user-1"), eq("yt-playlist-99"), eq(List.of("yt-1"))))
                .thenReturn(Mono.just(Set.of("yt-1")));

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
        verify(ytmusicOperations, times(1)).addTracksToPlaylistWithResults(
                "user-1", "yt-playlist-99", List.of("yt-1"));
    }

    @Test
    void rollsBackPlaylistWhenFewerThanNinetyPercentOfMatchedTracksAreInserted() {
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

        List<TransferItem> items = new java.util.ArrayList<>(java.util.stream.IntStream.range(0, 10)
                .mapToObj(index -> TransferItem.builder()
                        .id(UUID.randomUUID())
                        .jobId(jobId)
                        .sourceTrackId("source-" + index)
                        .targetTrackId("target-" + index)
                        .status(ItemStatus.PENDING.name())
                        .build())
                .toList());
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(jobRepository.findBySessionId(sessionId)).thenReturn(List.of(job));
        when(itemRepository.findByJobId(jobId)).thenReturn(items);

        CreatePlaylistResponseDto created = CreatePlaylistResponseDto.builder()
                .playlistId("target-playlist")
                .playlistUrl("https://music.youtube.com/playlist?list=target-playlist")
                .name("Top Hits (PlaySwap)")
                .build();
        when(ytmusicOperations.createPlaylist(eq("user-1"), anyString(), anyString()))
                .thenReturn(Mono.just(created));
        when(ytmusicOperations.addTracksToPlaylistWithResults(
                eq("user-1"), eq("target-playlist"), anyList()))
                .thenReturn(Mono.just(Set.of("target-0", "target-1", "target-2", "target-3",
                        "target-4", "target-5", "target-6", "target-7")));
        when(ytmusicOperations.deletePlaylist("user-1", "target-playlist"))
                .thenReturn(Mono.just(true));

        ExecuteTransferRequest request = ExecuteTransferRequest.builder()
                .sessionId(sessionId)
                .build();

        StepVerifier.create(executionService.executeTransfer("user-1", request))
                .assertNext(response -> {
                    assertEquals(TransferStatus.FAILED.name(), response.getStatus());
                    assertEquals(0, response.getTotalTransferredTracks());
                    assertNull(response.getTransferredPlaylists().get(0).getTargetPlaylistId());
                })
                .verifyComplete();

        assertEquals(TransferStatus.FAILED.name(), job.getStatus());
        assertTrue(items.stream().allMatch(item -> ItemStatus.FAILED.name().equals(item.getStatus())));
        verify(ytmusicOperations).deletePlaylist("user-1", "target-playlist");
    }

    @Test
    void keepsPlaylistWhenAtLeastNinetyPercentOfMatchedTracksAreInserted() {
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

        List<TransferItem> items = new java.util.ArrayList<>(java.util.stream.IntStream.range(0, 10)
                .mapToObj(index -> TransferItem.builder()
                        .id(UUID.randomUUID())
                        .jobId(jobId)
                        .sourceTrackId("source-" + index)
                        .targetTrackId("target-" + index)
                        .status(ItemStatus.PENDING.name())
                        .build())
                .toList());
        TransferItem unmatchedItem = TransferItem.builder()
                .id(UUID.randomUUID())
                .jobId(jobId)
                .sourceTrackId("unmatched-source")
                .status(ItemStatus.SKIPPED.name())
                .build();
        items.add(unmatchedItem);
        when(sessionRepository.findById(sessionId)).thenReturn(Optional.of(session));
        when(jobRepository.findBySessionId(sessionId)).thenReturn(List.of(job));
        when(itemRepository.findByJobId(jobId)).thenReturn(items);

        CreatePlaylistResponseDto created = CreatePlaylistResponseDto.builder()
                .playlistId("target-playlist")
                .playlistUrl("https://music.youtube.com/playlist?list=target-playlist")
                .name("Top Hits (PlaySwap)")
                .build();
        when(ytmusicOperations.createPlaylist(eq("user-1"), anyString(), anyString()))
                .thenReturn(Mono.just(created));
        when(ytmusicOperations.addTracksToPlaylistWithResults(
                eq("user-1"), eq("target-playlist"), anyList()))
                .thenReturn(Mono.just(Set.of("target-0", "target-1", "target-2", "target-3",
                        "target-4", "target-5", "target-6", "target-7", "target-8")));

        ExecuteTransferRequest request = ExecuteTransferRequest.builder()
                .sessionId(sessionId)
                .build();

        StepVerifier.create(executionService.executeTransfer("user-1", request))
                .assertNext(response -> {
                    assertEquals(TransferStatus.PARTIALLY_COMPLETED.name(), response.getStatus());
                    assertEquals(9, response.getTotalTransferredTracks());
                })
                .verifyComplete();

        assertEquals(TransferStatus.PARTIALLY_COMPLETED.name(), job.getStatus());
        assertEquals(ItemStatus.FAILED.name(), items.get(9).getStatus());
        assertEquals(ItemStatus.SKIPPED.name(), unmatchedItem.getStatus());
        verify(ytmusicOperations, never()).deletePlaylist(anyString(), anyString());
    }
}
