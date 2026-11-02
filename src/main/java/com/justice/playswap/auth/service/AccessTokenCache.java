package com.justice.playswap.auth.service;

import com.justice.playswap.auth.entity.AccessToken;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class AccessTokenCache {

    private static final long EXPIRY_SKEW_SECONDS = 300;

    private final ConcurrentHashMap<TokenKey, CacheEntry> tokens = new ConcurrentHashMap<>();

    public Optional<String> get(UUID userId, String platform) {
        TokenKey key = new TokenKey(userId, normalizePlatform(platform));
        CacheEntry entry = tokens.get(key);
        if (entry == null) {
            return Optional.empty();
        }

        if (entry.expiresAt().isAfter(Instant.now().plusSeconds(EXPIRY_SKEW_SECONDS))) {
            return Optional.of(entry.value());
        }

        tokens.remove(key, entry);
        return Optional.empty();
    }

    public void put(AccessToken token) {
        if (token == null || token.getAccessToken() == null || token.getAccessToken().isBlank()
                || token.getExpiresAt() == null
                || !token.getExpiresAt().isAfter(Instant.now().plusSeconds(EXPIRY_SKEW_SECONDS))) {
            return;
        }

        tokens.put(new TokenKey(token.getUserId(), normalizePlatform(token.getPlatform())),
                new CacheEntry(token.getAccessToken(), token.getExpiresAt()));
    }

    public void invalidate(UUID userId, String platform) {
        tokens.remove(new TokenKey(userId, normalizePlatform(platform)));
    }

    public void invalidate(UUID userId, String platform, String expectedToken) {
        TokenKey key = new TokenKey(userId, normalizePlatform(platform));
        CacheEntry entry = tokens.get(key);
        if (entry != null && entry.value().equals(expectedToken)) {
            tokens.remove(key, entry);
        }
    }

    private String normalizePlatform(String platform) {
        String normalized = platform == null ? "" : platform.trim().toLowerCase(Locale.ROOT);
        return normalized.equals("google") ? "youtube" : normalized;
    }

    private record TokenKey(UUID userId, String platform) {
    }

    private record CacheEntry(String value, Instant expiresAt) {
    }
}
