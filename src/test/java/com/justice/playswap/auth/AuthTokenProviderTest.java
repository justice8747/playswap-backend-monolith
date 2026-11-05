package com.justice.playswap.auth;

import com.justice.playswap.auth.entity.AccessToken;
import com.justice.playswap.auth.service.AccessTokenCache;
import com.justice.playswap.auth.service.AuthTokenProvider;
import com.justice.playswap.auth.service.TokenRefreshService;
import com.justice.playswap.auth.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class AuthTokenProviderTest {

    private final UserService userService = mock(UserService.class);
    private final TokenRefreshService tokenRefreshService = mock(TokenRefreshService.class);
    private final AccessTokenCache cache = new AccessTokenCache();
    private AuthTokenProvider tokenProvider;

    @BeforeEach
    void setUp() {
        tokenProvider = new AuthTokenProvider(userService, tokenRefreshService, cache);
    }

    @Test
    void loadsDatabaseOnceWhileTokenRemainsCached() {
        UUID userId = UUID.randomUUID();
        when(userService.getTokens(userId, "spotify"))
                .thenReturn(Mono.just(token(userId, "spotify", "valid-token", Instant.now().plusSeconds(3600))));

        StepVerifier.create(tokenProvider.getValidAccessToken(userId.toString(), "spotify"))
                .expectNext("valid-token")
                .verifyComplete();
        StepVerifier.create(tokenProvider.getValidAccessToken(userId.toString(), "spotify"))
                .expectNext("valid-token")
                .verifyComplete();

        verify(userService, times(1)).getTokens(userId, "spotify");
        verifyNoInteractions(tokenRefreshService);
    }

    @Test
    void refreshesExpiringTokenAndCachesRefreshedValue() {
        UUID userId = UUID.randomUUID();
        AccessToken expiring = token(userId, "spotify", "old-token", Instant.now().plusSeconds(60));
        AccessToken refreshed = token(userId, "spotify", "new-token", Instant.now().plusSeconds(3600));
        when(userService.getTokens(userId, "spotify")).thenReturn(Mono.just(expiring));
        when(tokenRefreshService.refresh(expiring, "spotify")).thenReturn(Mono.just(refreshed));

        StepVerifier.create(tokenProvider.getValidAccessToken(userId.toString(), "spotify"))
                .expectNext("new-token")
                .verifyComplete();
        StepVerifier.create(tokenProvider.getValidAccessToken(userId.toString(), "spotify"))
                .expectNext("new-token")
                .verifyComplete();

        verify(userService, times(1)).getTokens(userId, "spotify");
        verify(tokenRefreshService, times(1)).refresh(expiring, "spotify");
    }

    @Test
    void refreshesAndRetriesProviderOperationOnceAfterUnauthorizedResponse() {
        UUID userId = UUID.randomUUID();
        AccessToken stored = token(userId, "spotify", "rejected-token", Instant.now().plusSeconds(3600));
        AccessToken refreshed = token(userId, "spotify", "recovered-token", Instant.now().plusSeconds(3600));
        when(userService.getTokens(userId, "spotify")).thenReturn(Mono.just(stored));
        when(tokenRefreshService.refresh(stored, "spotify")).thenReturn(Mono.just(refreshed));
        AtomicInteger operationCalls = new AtomicInteger();

        StepVerifier.create(tokenProvider.withValidAccessToken(userId.toString(), "spotify", accessToken -> {
                    operationCalls.incrementAndGet();
                    if (accessToken.equals("rejected-token")) {
                        return Mono.error(unauthorized());
                    }
                    return Mono.just(accessToken);
                }))
                .expectNext("recovered-token")
                .verifyComplete();

        verify(userService, times(2)).getTokens(userId, "spotify");
        verify(tokenRefreshService, times(1)).refresh(stored, "spotify");
        org.junit.jupiter.api.Assertions.assertEquals(2, operationCalls.get());
    }

    @Test
    void refreshesButDoesNotReplayNonIdempotentOperationAfterUnauthorizedResponse() {
        UUID userId = UUID.randomUUID();
        AccessToken stored = token(userId, "spotify", "rejected-token", Instant.now().plusSeconds(3600));
        AccessToken refreshed = token(userId, "spotify", "recovered-token", Instant.now().plusSeconds(3600));
        when(userService.getTokens(userId, "spotify")).thenReturn(Mono.just(stored));
        when(tokenRefreshService.refresh(stored, "spotify")).thenReturn(Mono.just(refreshed));
        AtomicInteger operationCalls = new AtomicInteger();

        StepVerifier.create(tokenProvider.withValidAccessTokenWithoutRetry(
                        userId.toString(), "spotify", accessToken -> {
                            operationCalls.incrementAndGet();
                            return Mono.error(unauthorized());
                        }))
                .expectError(WebClientResponseException.class)
                .verify();

        StepVerifier.create(tokenProvider.getValidAccessToken(userId.toString(), "spotify"))
                .expectNext("recovered-token")
                .verifyComplete();

        verify(tokenRefreshService, times(1)).refresh(stored, "spotify");
        org.junit.jupiter.api.Assertions.assertEquals(1, operationCalls.get());
    }

    @Test
    void keepsTokensForDifferentPlatformsIndependent() {
        UUID userId = UUID.randomUUID();
        when(userService.getTokens(userId, "spotify"))
                .thenReturn(Mono.just(token(userId, "spotify", "spotify-token", Instant.now().plusSeconds(3600))));
        when(userService.getTokens(userId, "youtube"))
                .thenReturn(Mono.just(token(userId, "youtube", "youtube-token", Instant.now().plusSeconds(3600))));

        StepVerifier.create(tokenProvider.getValidAccessToken(userId.toString(), "spotify"))
                .expectNext("spotify-token")
                .verifyComplete();
        StepVerifier.create(tokenProvider.getValidAccessToken(userId.toString(), "youtube"))
                .expectNext("youtube-token")
                .verifyComplete();

        verify(userService).getTokens(userId, "spotify");
        verify(userService).getTokens(userId, "youtube");
    }

    private AccessToken token(UUID userId, String platform, String value, Instant expiresAt) {
        return AccessToken.builder()
                .userId(userId)
                .platform(platform)
                .accessToken(value)
                .refreshToken("refresh-token")
                .expiresAt(expiresAt)
                .build();
    }

    private WebClientResponseException unauthorized() {
        return WebClientResponseException.create(
                401,
                "Unauthorized",
                HttpHeaders.EMPTY,
                new byte[0],
                StandardCharsets.UTF_8);
    }
}
