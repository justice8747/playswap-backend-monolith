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
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;
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
                Map.of("videoId", "A1B2C3D4E5F", "title", "Starboy", "channel", "The Weeknd")
        );

        when(innerTubeClient.searchSongs(anyString())).thenReturn(Mono.just(innerTubeCandidates));

        StepVerifier.create(trackMatchingService.analyzeTrack(request, "dummy-token"))
                .assertNext(result -> {
                    assertTrue(result.isMatched());
                    assertEquals("A1B2C3D4E5F", result.getYoutubeVideoId());
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
                Map.of("videoId", "R4r3Unr3l1X", "title", "Rare Unreleased Track", "channel", "Obscure Artist")
        );
        when(innerTubeClient.searchVideos(anyString())).thenReturn(Mono.just(videoCandidates));

        StepVerifier.create(trackMatchingService.analyzeTrack(request, null))
                .assertNext(result -> {
                    assertTrue(result.isMatched());
                    assertEquals("R4r3Unr3l1X", result.getYoutubeVideoId());
                    assertTrue(result.getConfidence() >= 55);
                })
                .verifyComplete();

        verify(innerTubeClient, times(2)).searchSongs(anyString());
        verify(innerTubeClient, times(1)).searchVideos(anyString());
    }

    @Test
    void continuesToTier3WhenTier2RequestFails() {
        TrackAnalysisRequest request = TrackAnalysisRequest.builder()
                .spotifyTrackId("sp-track-3")
                .title("Song Found In Video Search")
                .artist("Artist")
                .primaryArtist("Artist")
                .build();
        List<Map<String, String>> candidates = List.of(
                Map.of("videoId", "XyZ123abc_1", "title", "Song Found In Video Search", "channel", "Artist")
        );

        when(innerTubeClient.searchSongs(anyString()))
                .thenReturn(Mono.error(new IllegalStateException("Tier 2 unavailable")));
        when(innerTubeClient.searchVideos(anyString())).thenReturn(Mono.just(candidates));

        StepVerifier.create(trackMatchingService.analyzeTrack(request, null))
                .assertNext(result -> {
                    assertTrue(result.isMatched());
                    assertEquals("XyZ123abc_1", result.getYoutubeVideoId());
                })
                .verifyComplete();

        verify(innerTubeClient, times(2)).searchSongs(anyString());
        verify(innerTubeClient, times(1)).searchVideos(anyString());
    }

    @Test
    void continuesThroughTier3AndTier4WhenInnerTubeCompletesEmpty() {
        TrackAnalysisRequest request = TrackAnalysisRequest.builder()
                .spotifyTrackId("sp-track-empty")
                .title("Official Fallback Track")
                .artist("Fallback Artist")
                .primaryArtist("Fallback Artist")
                .build();
        String officialApiResponse = """
                {
                  "items": [{
                    "id": {"kind": "youtube#video", "videoId": "Of1c1alF4ll"},
                    "snippet": {
                      "title": "Official Fallback Track",
                      "channelTitle": "Fallback Artist"
                    }
                  }]
                }
                """;
        WebClient.Builder webClientBuilder = WebClient.builder()
                .exchangeFunction(requestMessage -> Mono.just(
                        ClientResponse.create(HttpStatus.OK)
                                .header("Content-Type", "application/json")
                                .body(officialApiResponse)
                                .build()));
        TrackMatchingService service = new TrackMatchingService(innerTubeClient, webClientBuilder);

        when(innerTubeClient.searchSongs(anyString())).thenReturn(Mono.empty());
        when(innerTubeClient.searchVideos(anyString())).thenReturn(Mono.empty());

        StepVerifier.create(service.analyzeTrack(request, "youtube-token"))
                .assertNext(result -> {
                    assertTrue(result.isMatched());
                    assertEquals("Of1c1alF4ll", result.getYoutubeVideoId());
                })
                .verifyComplete();

        verify(innerTubeClient, times(2)).searchSongs(anyString());
        verify(innerTubeClient, times(2)).searchVideos(anyString());
    }

    @Test
    void retriesInnerTubeWithTitleOnlyBeforeUsingOfficialApi() {
        TrackAnalysisRequest request = TrackAnalysisRequest.builder()
                .spotifyTrackId("sp-track-5")
                .title("Title Only Result")
                .artist("Artist Name")
                .primaryArtist("Artist Name")
                .build();
        List<Map<String, String>> candidates = List.of(
                Map.of("videoId", "T1tle0nly11", "title", "Title Only Result (Official Audio)", "channel", "Artist Name - Topic")
        );

        when(innerTubeClient.searchSongs("Title Only Result Artist Name")).thenReturn(Mono.just(List.of()));
        when(innerTubeClient.searchSongs("Title Only Result")).thenReturn(Mono.just(candidates));

        StepVerifier.create(trackMatchingService.analyzeTrack(request, "unused-token"))
                .assertNext(result -> {
                    assertTrue(result.isMatched());
                    assertEquals("T1tle0nly11", result.getYoutubeVideoId());
                    assertEquals(100, result.getConfidence());
                })
                .verifyComplete();

        verify(innerTubeClient, times(2)).searchSongs(anyString());
        verify(innerTubeClient, never()).searchVideos(anyString());
    }

    @Test
    void retriesTier3WithTitleOnlyBeforeUsingOfficialApi() {
        TrackAnalysisRequest request = TrackAnalysisRequest.builder()
                .spotifyTrackId("sp-track-8")
                .title("Title Found In Video Search")
                .artist("Artist Name")
                .primaryArtist("Artist Name")
                .build();
        List<Map<String, String>> candidates = List.of(
                Map.of("videoId", "T1tleF0und1", "title", "Title Found In Video Search", "channel", "Artist Name")
        );

        when(innerTubeClient.searchSongs(anyString())).thenReturn(Mono.just(List.of()));
        when(innerTubeClient.searchVideos(anyString()))
                .thenReturn(Mono.just(List.of()), Mono.just(candidates));

        StepVerifier.create(trackMatchingService.analyzeTrack(request, null))
                .assertNext(result -> {
                    assertTrue(result.isMatched());
                    assertEquals("T1tleF0und1", result.getYoutubeVideoId());
                })
                .verifyComplete();

        verify(innerTubeClient, times(2)).searchSongs(anyString());
        verify(innerTubeClient, times(2)).searchVideos(anyString());
    }

    @Test
    void rejectsMatchWhenArtistEvidenceIsMissing() {
        TrackAnalysisRequest request = TrackAnalysisRequest.builder()
                .spotifyTrackId("sp-track-6")
                .title("Exact Song Title")
                .artist("Artist Name")
                .primaryArtist("Artist Name")
                .build();
        when(innerTubeClient.searchSongs(anyString())).thenReturn(Mono.just(List.of(
                Map.of("videoId", "Ex4ctSong11", "title", "Exact Song Title", "channel", "")
        )));
        when(innerTubeClient.searchVideos(anyString())).thenReturn(Mono.just(List.of()));

        StepVerifier.create(trackMatchingService.analyzeTrack(request, null))
                .assertNext(result -> {
                    assertFalse(result.isMatched());
                    assertEquals("LOW_CONFIDENCE", result.getFailureReason());
                })
                .verifyComplete();
    }

    @Test
    void rejectsAmbiguousInnerTubeCandidates() {
        TrackAnalysisRequest request = TrackAnalysisRequest.builder()
                .spotifyTrackId("sp-track-7")
                .title("Exact Song Title")
                .artist("Artist Name")
                .primaryArtist("Artist Name")
                .build();
        when(innerTubeClient.searchSongs(anyString())).thenReturn(Mono.just(List.of(
                Map.of("videoId", "Ex4ctSong11", "title", "Exact Song Title", "channel", "Artist Name"),
                Map.of("videoId", "Ex4ctSong22", "title", "Exact Song Title", "channel", "Artist Name")
        )));
        when(innerTubeClient.searchVideos(anyString())).thenReturn(Mono.just(List.of()));

        StepVerifier.create(trackMatchingService.analyzeTrack(request, null))
                .assertNext(result -> {
                    assertFalse(result.isMatched());
                    assertEquals("AMBIGUOUS_MATCH", result.getFailureReason());
                })
                .verifyComplete();
    }

    @Test
    void reportsAnalysisErrorWhenInnerTubeTiersFail() {
        TrackAnalysisRequest request = TrackAnalysisRequest.builder()
                .spotifyTrackId("sp-track-4")
                .title("Unavailable Song")
                .artist("Artist")
                .primaryArtist("Artist")
                .build();

        when(innerTubeClient.searchSongs(anyString()))
                .thenReturn(Mono.error(new IllegalStateException("Tier 2 unavailable")));
        when(innerTubeClient.searchVideos(anyString()))
                .thenReturn(Mono.error(new IllegalStateException("Tier 3 unavailable")));

        StepVerifier.create(trackMatchingService.analyzeTrack(request, null))
                .assertNext(result -> {
                    assertFalse(result.isMatched());
                    assertEquals("ANALYSIS_ERROR", result.getFailureReason());
                })
                .verifyComplete();
    }
}
