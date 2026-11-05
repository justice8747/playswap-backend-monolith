package com.justice.playswap.auth.service;

import com.justice.playswap.auth.entity.RevokedJwt;
import com.justice.playswap.auth.repository.RevokedJwtRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class JwtRevocationService {

    private final RevokedJwtRepository revokedJwtRepository;
    private final AccessTokenCache accessTokenCache;

    public Mono<Void> revoke(Jwt jwt) {
        if (jwt == null) {
            return Mono.empty();
        }

        Instant expiresAt = jwt.getExpiresAt();
        if (expiresAt == null || !expiresAt.isAfter(Instant.now())) {
            return Mono.error(new IllegalArgumentException("Cannot revoke a JWT without a future expiration"));
        }

        String tokenHash = hash(jwt.getTokenValue());
        return Mono.fromRunnable(() -> {
            revokedJwtRepository.save(RevokedJwt.builder()
                    .tokenHash(tokenHash)
                    .expiresAt(expiresAt)
                    .revokedAt(Instant.now())
                    .build());
            evictPlatformTokenCache(jwt);
            revokedJwtRepository.deleteAllByExpiresAtBefore(Instant.now());
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    public Mono<Boolean> isRevoked(String tokenValue) {
        String tokenHash = hash(tokenValue);
        return Mono.fromCallable(() -> revokedJwtRepository
                        .existsByTokenHashAndExpiresAtAfter(tokenHash, Instant.now()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    private void evictPlatformTokenCache(Jwt jwt) {
        String userId = jwt.getSubject();
        if (userId == null || userId.isBlank()) {
            return;
        }
        try {
            var userUuid = java.util.UUID.fromString(userId);
            accessTokenCache.invalidate(userUuid, "spotify");
            accessTokenCache.invalidate(userUuid, "youtube");
        } catch (IllegalArgumentException error) {
            log.warn("JWT subject is not a UUID; skipping provider token cache eviction");
        }
    }

    private String hash(String tokenValue) {
        if (tokenValue == null || tokenValue.isBlank()) {
            throw new IllegalArgumentException("JWT value must not be blank");
        }
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(tokenValue.getBytes(StandardCharsets.UTF_8));
            return java.util.HexFormat.of().formatHex(digest);
        } catch (java.security.NoSuchAlgorithmException error) {
            throw new IllegalStateException("SHA-256 is unavailable", error);
        }
    }
}
