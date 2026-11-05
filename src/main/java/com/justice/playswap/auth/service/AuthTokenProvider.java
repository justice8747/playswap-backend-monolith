package com.justice.playswap.auth.service;

import com.justice.playswap.common.provider.TokenProvider;
import com.justice.playswap.auth.entity.AccessToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.UUID;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthTokenProvider implements TokenProvider {

    private final UserService userService;
    private final TokenRefreshService tokenRefreshService;
    private final AccessTokenCache accessTokenCache;
    private final ConcurrentHashMap<TokenKey, Mono<String>> tokenLoads = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<TokenKey, Mono<String>> unauthorizedRecoveries = new ConcurrentHashMap<>();

    @Override
    public Mono<String> getValidAccessToken(String userIdStr, String platform) {
        return resolveKey(userIdStr, platform)
                .flatMap(key -> getOrLoadToken(key, userIdStr));
    }

    @Override
    public <T> Mono<T> withValidAccessToken(
            String userIdStr,
            String platform,
            Function<String, Mono<T>> operation) {
        return withValidAccessToken(userIdStr, platform, operation, true);
    }

    @Override
    public <T> Mono<T> withValidAccessTokenWithoutRetry(
            String userIdStr,
            String platform,
            Function<String, Mono<T>> operation) {
        return withValidAccessToken(userIdStr, platform, operation, false);
    }

    private <T> Mono<T> withValidAccessToken(
            String userIdStr,
            String platform,
            Function<String, Mono<T>> operation,
            boolean retryUnauthorized) {
        return resolveKey(userIdStr, platform)
                .flatMap(key -> getOrLoadToken(key, userIdStr)
                        .flatMap(accessToken -> Mono.defer(() -> operation.apply(accessToken))
                                .onErrorResume(error -> {
                                    if (!isUnauthorized(error)) {
                                        return Mono.error(error);
                                    }
                                    return recoverUnauthorized(key, accessToken)
                                            .flatMap(refreshedToken -> retryUnauthorized
                                                    ? Mono.defer(() -> operation.apply(refreshedToken))
                                                    : Mono.error(error));
                                })));
    }

    private Mono<TokenKey> resolveKey(String userIdStr, String platform) {
        if (userIdStr == null || userIdStr.isBlank()) {
            return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid user ID"));
        }

        UUID userUuid;
        try {
            userUuid = UUID.fromString(userIdStr);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID format for userId: {}", userIdStr);
            return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid user ID: " + userIdStr));
        }

        String normalizedPlatform = normalizePlatform(platform);
        if (!normalizedPlatform.equals("spotify") && !normalizedPlatform.equals("youtube")) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported platform: " + platform));
        }
        return Mono.just(new TokenKey(userUuid, normalizedPlatform));
    }

    private Mono<String> getOrLoadToken(TokenKey key, String originalUserId) {
        return accessTokenCache.get(key.userId(), key.platform())
                .map(Mono::just)
                .orElseGet(() -> tokenLoads.computeIfAbsent(key, ignored ->
                        loadToken(key, originalUserId)
                                .doFinally(signal -> tokenLoads.remove(key))
                                .cache()));
    }

    private Mono<String> loadToken(TokenKey key, String originalUserId) {
        return userService.getTokens(key.userId(), key.platform())
                .flatMap(tokenRecord -> {
                    if (needsRefresh(tokenRecord)) {
                        log.info("Token for user {} on platform {} is expired/expiring; refreshing",
                                key.userId(), key.platform());
                        return tokenRefreshService.refresh(tokenRecord, key.platform())
                                .doOnNext(accessTokenCache::put)
                                .map(AccessToken::getAccessToken)
                                .onErrorResume(error -> {
                                    log.error("Failed to refresh {} token for user {}: {}. Falling back to existing access token.",
                                            key.platform(), key.userId(), error.getMessage());
                                    if (tokenRecord.getAccessToken() != null && !tokenRecord.getAccessToken().isBlank()) {
                                        return Mono.just(tokenRecord.getAccessToken());
                                    }
                                    return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Failed to refresh token"));
                                });
                    }

                    accessTokenCache.put(tokenRecord);
                    return Mono.just(tokenRecord.getAccessToken());
                })
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        key.platform() + " access token not found for user: " + originalUserId)));
    }

    private Mono<String> recoverUnauthorized(TokenKey key, String rejectedToken) {
        return unauthorizedRecoveries.computeIfAbsent(key, ignored ->
                recoverUnauthorizedToken(key, rejectedToken)
                        .doFinally(signal -> unauthorizedRecoveries.remove(key))
                        .cache());
    }

    private Mono<String> recoverUnauthorizedToken(TokenKey key, String rejectedToken) {
        accessTokenCache.invalidate(key.userId(), key.platform(), rejectedToken);
        var cachedToken = accessTokenCache.get(key.userId(), key.platform());
        if (cachedToken.isPresent() && !cachedToken.get().equals(rejectedToken)) {
            return Mono.just(cachedToken.get());
        }

        return userService.getTokens(key.userId(), key.platform())
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                        "No token available to recover from provider unauthorized response")))
                .flatMap(tokenRecord -> {
                    boolean databaseHasNewToken = tokenRecord.getAccessToken() != null
                            && !tokenRecord.getAccessToken().equals(rejectedToken)
                            && !needsRefresh(tokenRecord);
                    if (databaseHasNewToken) {
                        accessTokenCache.put(tokenRecord);
                        return Mono.just(tokenRecord.getAccessToken());
                    }
                    if (tokenRecord.getRefreshToken() == null || tokenRecord.getRefreshToken().isBlank()) {
                        return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                                "Provider rejected access token and no refresh token is available"));
                    }
                    return tokenRefreshService.refresh(tokenRecord, key.platform())
                            .doOnNext(accessTokenCache::put)
                            .map(AccessToken::getAccessToken);
                });
    }

    private boolean needsRefresh(AccessToken tokenRecord) {
        return tokenRecord.getExpiresAt() == null
                || !tokenRecord.getExpiresAt().isAfter(Instant.now().plusSeconds(300));
    }

    private boolean isUnauthorized(Throwable error) {
        Throwable current = error;
        while (current != null) {
            if (current instanceof WebClientResponseException webClientError
                    && webClientError.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return true;
            }
            if (current instanceof ResponseStatusException responseStatusError
                    && responseStatusError.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private String normalizePlatform(String platform) {
        String normalized = platform == null ? "" : platform.trim().toLowerCase(Locale.ROOT);
        return normalized.equals("google") ? "youtube" : normalized;
    }

    private record TokenKey(UUID userId, String platform) {
    }
}
