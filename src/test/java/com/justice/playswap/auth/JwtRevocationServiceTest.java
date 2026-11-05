package com.justice.playswap.auth;

import com.justice.playswap.auth.entity.RevokedJwt;
import com.justice.playswap.auth.repository.RevokedJwtRepository;
import com.justice.playswap.auth.service.AccessTokenCache;
import com.justice.playswap.auth.service.JwtRevocationService;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import reactor.test.StepVerifier;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class JwtRevocationServiceTest {

    private final RevokedJwtRepository repository = mock(RevokedJwtRepository.class);
    private final AccessTokenCache accessTokenCache = mock(AccessTokenCache.class);
    private final JwtRevocationService service = new JwtRevocationService(repository, accessTokenCache);

    @Test
    void storesOnlyTokenHashAndEvictsPlatformTokenCache() {
        UUID userId = UUID.randomUUID();
        Jwt jwt = Jwt.withTokenValue("secret-jwt-value")
                .header("alg", "HS256")
                .subject(userId.toString())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        StepVerifier.create(service.revoke(jwt)).verifyComplete();

        var revokedJwtCaptor = org.mockito.ArgumentCaptor.forClass(RevokedJwt.class);
        verify(repository).save(revokedJwtCaptor.capture());
        RevokedJwt revokedJwt = revokedJwtCaptor.getValue();
        assertEquals(64, revokedJwt.getTokenHash().length());
        org.junit.jupiter.api.Assertions.assertNotEquals("secret-jwt-value", revokedJwt.getTokenHash());
        verify(accessTokenCache).invalidate(userId, "spotify");
        verify(accessTokenCache).invalidate(userId, "youtube");
        verify(repository).deleteAllByExpiresAtBefore(any(Instant.class));

        when(repository.existsByTokenHashAndExpiresAtAfter(
                eq(revokedJwt.getTokenHash()), any(Instant.class))).thenReturn(true);
        StepVerifier.create(service.isRevoked("secret-jwt-value"))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    void rejectsExpiredJwtRevocation() {
        Jwt expiredJwt = Jwt.withTokenValue("expired-jwt")
                .header("alg", "HS256")
                .subject(UUID.randomUUID().toString())
                .expiresAt(Instant.now().minusSeconds(1))
                .build();

        StepVerifier.create(service.revoke(expiredJwt))
                .expectError(IllegalArgumentException.class)
                .verify();

        verifyNoInteractions(repository);
    }
}
