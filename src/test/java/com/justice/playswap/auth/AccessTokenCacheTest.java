package com.justice.playswap.auth;

import com.justice.playswap.auth.entity.AccessToken;
import com.justice.playswap.auth.service.AccessTokenCache;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AccessTokenCacheTest {

    private final AccessTokenCache cache = new AccessTokenCache();

    @Test
    void cachesSpotifyAndYoutubeTokensIndependentlyForSameUser() {
        UUID userId = UUID.randomUUID();
        cache.put(token(userId, "spotify", "spotify-token", Instant.now().plusSeconds(3600)));
        cache.put(token(userId, "youtube", "youtube-token", Instant.now().plusSeconds(3600)));

        assertEquals("spotify-token", cache.get(userId, "spotify").orElseThrow());
        assertEquals("youtube-token", cache.get(userId, "youtube").orElseThrow());
        assertEquals("youtube-token", cache.get(userId, "google").orElseThrow());
    }

    @Test
    void doesNotReturnTokenWithinExpirySkew() {
        UUID userId = UUID.randomUUID();
        cache.put(token(userId, "spotify", "soon-expiring", Instant.now().plusSeconds(240)));

        assertTrue(cache.get(userId, "spotify").isEmpty());
    }

    private AccessToken token(UUID userId, String platform, String value, Instant expiresAt) {
        return AccessToken.builder()
                .userId(userId)
                .platform(platform)
                .accessToken(value)
                .expiresAt(expiresAt)
                .build();
    }
}
