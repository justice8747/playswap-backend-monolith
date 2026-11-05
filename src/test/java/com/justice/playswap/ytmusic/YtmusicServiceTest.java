package com.justice.playswap.ytmusic;

import com.justice.playswap.common.provider.TokenProvider;
import com.justice.playswap.transfer.dto.SpotifyTrackDto;
import com.justice.playswap.ytmusic.client.YoutubeApiClient;
import com.justice.playswap.ytmusic.model.TrackAnalysisRequest;
import com.justice.playswap.ytmusic.model.TrackAnalysisResult;
import com.justice.playswap.ytmusic.service.TrackMatchingService;
import com.justice.playswap.ytmusic.service.YtmusicService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

class YtmusicServiceTest {

    private final TokenProvider tokenProvider = mock(TokenProvider.class);
    private final YoutubeApiClient youtubeApiClient = mock(YoutubeApiClient.class);
    private final TrackMatchingService trackMatchingService = mock(TrackMatchingService.class);
    private final YtmusicService ytmusicService =
            new YtmusicService(tokenProvider, youtubeApiClient, trackMatchingService);

    @Test
    void fallsBackToInnerTubeWhenYoutubeTokenCannotBeLoaded() {
        when(tokenProvider.getValidAccessToken("user-1", "youtube"))
                .thenReturn(Mono.error(new IllegalStateException("No linked YouTube account")));
        when(trackMatchingService.analyzeTrack(any(TrackAnalysisRequest.class), isNull(String.class)))
                .thenReturn(Mono.just(failedResult()));

        StepVerifier.create(ytmusicService.analyzeTrack("user-1", track()))
                .assertNext(result -> {
                    assertFalse(result.isMatched());
                    assertEquals("NO_CANDIDATES", result.getFailureReason());
                })
                .verifyComplete();

        verify(trackMatchingService).analyzeTrack(any(TrackAnalysisRequest.class), isNull(String.class));
        verify(tokenProvider, never()).withValidAccessToken(eq("user-1"), eq("youtube"), any());
    }

    @Test
    void doesNotRetryOfficialApiFailureWithoutToken() {
        when(tokenProvider.getValidAccessToken("user-1", "youtube")).thenReturn(Mono.just("token"));
        when(tokenProvider.withValidAccessToken(eq("user-1"), eq("youtube"), any()))
                .thenReturn(Mono.error(forbidden()));

        StepVerifier.create(ytmusicService.analyzeTrack("user-1", track()))
                .expectError(WebClientResponseException.class)
                .verify();

        verify(trackMatchingService, never())
                .analyzeTrack(any(TrackAnalysisRequest.class), isNull(String.class));
    }

    @Test
    void obtainsTokenSeparatelyForEachPlaylistItem() {
        when(tokenProvider.<Set<String>>withValidAccessToken(eq("user-1"), eq("youtube"), any()))
                .thenAnswer(invocation -> {
                    Function<String, Mono<Set<String>>> operation = invocation.getArgument(2);
                    return operation.apply("token");
                });
        when(youtubeApiClient.addTracksToPlaylistWithResults(eq("playlist-1"), any(), eq("token")))
                .thenAnswer(invocation -> Mono.just(Set.copyOf(invocation.getArgument(1))));

        StepVerifier.create(ytmusicService.addTracksToPlaylistWithResults(
                        "user-1", "playlist-1", List.of("video-1", "video-2")))
                .expectNext(Set.of("video-1", "video-2"))
                .verifyComplete();

        verify(tokenProvider, times(2))
                .withValidAccessToken(eq("user-1"), eq("youtube"), any());
        verify(youtubeApiClient).addTracksToPlaylistWithResults(
                "playlist-1", List.of("video-1"), "token");
        verify(youtubeApiClient).addTracksToPlaylistWithResults(
                "playlist-1", List.of("video-2"), "token");
    }

    private SpotifyTrackDto track() {
        return SpotifyTrackDto.builder()
                .trackId("track-1")
                .title("Title")
                .artists("Artist")
                .primaryArtist("Artist")
                .build();
    }

    private TrackAnalysisResult failedResult() {
        return TrackAnalysisResult.builder()
                .spotifyTrackId("track-1")
                .title("Title")
                .artist("Artist")
                .matched(false)
                .failureReason("NO_CANDIDATES")
                .build();
    }

    private WebClientResponseException forbidden() {
        return WebClientResponseException.create(
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                HttpHeaders.EMPTY,
                new byte[0],
                null);
    }
}
