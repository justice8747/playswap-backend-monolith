package com.justice.playswap.ytmusic;

import com.justice.playswap.ytmusic.client.InnerTubeClient;
import com.justice.playswap.ytmusic.model.TrackAnalysisRequest;
import com.justice.playswap.ytmusic.model.TrackAnalysisResult;
import com.justice.playswap.ytmusic.service.TrackMatchingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackMatchingTest {

    @Mock
    private InnerTubeClient innerTubeClient;

    private TrackMatchingService trackMatchingService;

    @BeforeEach
    void setUp() {
        trackMatchingService = new TrackMatchingService(innerTubeClient, WebClient.builder());
    }

    @Test
    void testTier2InnerTubeSongMatchSuccess() {
        TrackAnalysisRequest request = TrackAnalysisRequest.builder()
                .spotifyTrackId("sp-track-1")
                .title("Starboy")
                .artist("The Weeknd")
                .primaryArtist("The Weeknd")
                .build();

        List<Map<String, String>> innerTubeCandidates = List.of(
                Map.of("videoId", "yt-vid-starboy", "title", "Starboy", "channel", "The Weeknd")
        );

        when(innerTubeClient.searchSongs(anyString())).thenReturn(Mono.just(innerTubeCandidates));

        StepVerifier.create(trackMatchingService.analyzeTrack(request, "dummy-token"))
                .assertNext(result -> {
                    assertTrue(result.isMatched());
                    assertEquals("yt-vid-starboy", result.getYoutubeVideoId());
                    assertTrue(result.getConfidence() >= 60);
                })
                .verifyComplete();

        verify(innerTubeClient, times(1)).searchSongs(anyString());
        verify(innerTubeClient, never()).searchVideos(anyString());
    }

    @Test
    void testTier3InnerTubeVideoFallbackWhenSongFails() {
        TrackAnalysisRequest request = TrackAnalysisRequest.builder()
                .spotifyTrackId("sp-track-2")
                .title("Rare Unreleased Track")
                .artist("Obscure Artist")
                .primaryArtist("Obscure Artist")
                .build();

        // Tier 2 returns no candidates
        when(innerTubeClient.searchSongs(anyString())).thenReturn(Mono.just(List.of()));

        // Tier 3 returns video candidate
        List<Map<String, String>> videoCandidates = List.of(
                Map.of("videoId", "yt-vid-rare", "title", "Rare Unreleased Track", "channel", "Obscure Artist")
        );
        when(innerTubeClient.searchVideos(anyString())).thenReturn(Mono.just(videoCandidates));

        StepVerifier.create(trackMatchingService.analyzeTrack(request, null))
                .assertNext(result -> {
                    assertTrue(result.isMatched());
                    assertEquals("yt-vid-rare", result.getYoutubeVideoId());
                    assertTrue(result.getConfidence() >= 55);
                })
                .verifyComplete();

        verify(innerTubeClient, times(1)).searchSongs(anyString());
        verify(innerTubeClient, times(1)).searchVideos(anyString());
    }
}
