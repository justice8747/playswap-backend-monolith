package com.justice.playswap.ytmusic;

import com.justice.playswap.common.provider.TokenProvider;
import com.justice.playswap.ytmusic.client.YoutubeApiClient;
import com.justice.playswap.ytmusic.model.TrackAnalysisRequest;
import com.justice.playswap.ytmusic.service.PlaylistCacheService;
import com.justice.playswap.ytmusic.service.TrackMatchingService;
import com.justice.playswap.ytmusic.controller.YtmusicController;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

class YtmusicControllerTest {

    private final TokenProvider tokenProvider = mock(TokenProvider.class);
    private final TrackMatchingService trackMatchingService = mock(TrackMatchingService.class);
    private final YtmusicController controller = new YtmusicController(
            tokenProvider,
            mock(YoutubeApiClient.class),
            mock(PlaylistCacheService.class),
            trackMatchingService);

    @Test
    void doesNotRetryOfficialApiFailureWithoutToken() {
        when(tokenProvider.getValidAccessToken("user-1", "youtube")).thenReturn(Mono.just("token"));
        when(tokenProvider.withValidAccessToken(eq("user-1"), eq("youtube"), any()))
                .thenReturn(Mono.error(forbidden()));

        StepVerifier.create(controller.analyzeTrack(null, "user-1", request()))
                .expectError(WebClientResponseException.class)
                .verify();

        verify(trackMatchingService, never())
                .analyzeTrack(any(TrackAnalysisRequest.class), isNull(String.class));
    }

    private TrackAnalysisRequest request() {
        return TrackAnalysisRequest.builder()
                .spotifyTrackId("track-1")
                .title("Title")
                .artist("Artist")
                .primaryArtist("Artist")
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
