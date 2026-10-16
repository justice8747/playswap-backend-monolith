package com.justice.playswap.auth.service;

import com.justice.playswap.common.provider.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthTokenProvider implements TokenProvider {

    private final UserService userService;
    private final TokenRefreshService tokenRefreshService;

    @Override
    public Mono<String> getValidAccessToken(String userIdStr, String platform) {
        log.info("==> [AuthTokenProvider] Resolving in-process token for userId: '{}' and platform: '{}'", userIdStr, platform);

        UUID userUuid;
        try {
            userUuid = UUID.fromString(userIdStr);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid UUID format for userId: {}", userIdStr);
            return Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid user ID: " + userIdStr));
        }

        return userService.getTokens(userUuid, platform)
                .flatMap(tokenRecord -> {
                    boolean needsRefresh = tokenRecord.getExpiresAt() == null ||
                            tokenRecord.getExpiresAt().isBefore(Instant.now().plusSeconds(300));

                    if (needsRefresh) {
                        log.info("Token for user {} on platform {} is expired/expiring. Refreshing in-process...", userUuid, platform);
                        return tokenRefreshService.refresh(tokenRecord, platform)
                                .map(refreshed -> refreshed.getAccessToken())
                                .onErrorResume(e -> {
                                    log.error("Failed to refresh {} token for user {}: {}. Falling back to existing access token.",
                                            platform, userUuid, e.getMessage());
                                    if (tokenRecord.getAccessToken() != null && !tokenRecord.getAccessToken().isBlank()) {
                                        return Mono.just(tokenRecord.getAccessToken());
                                    }
                                    return Mono.error(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Failed to refresh token"));
                                });
                    }

                    return Mono.just(tokenRecord.getAccessToken());
                })
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND,
                        platform + " access token not found for user: " + userIdStr)));
    }
}
