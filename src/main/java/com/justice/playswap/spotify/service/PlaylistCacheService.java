package com.justice.playswap.spotify.service;

import com.justice.playswap.spotify.model.SpotifyPlaylist;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service("spotifyPlaylistCacheService")
public class PlaylistCacheService {

    private static final long DEFAULT_TTL_SECONDS = 1800; // 30 minutes cache TTL

    private static class CacheEntry {
        final List<SpotifyPlaylist> playlists;
        final Instant expiryTime;

        CacheEntry(List<SpotifyPlaylist> playlists, long ttlSeconds) {
            this.playlists = playlists;
            this.expiryTime = Instant.now().plusSeconds(ttlSeconds);
        }

        boolean isExpired() {
            return Instant.now().isAfter(expiryTime);
        }
    }

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public List<SpotifyPlaylist> get(String userId) {
        CacheEntry entry = cache.get(userId);
        if (entry != null) {
            if (!entry.isExpired()) {
                log.info("Cache hit for user {} (Spotify playlists)", userId);
                return entry.playlists;
            } else {
                log.info("Cache expired for user {} (Spotify playlists)", userId);
                cache.remove(userId);
            }
        }
        return null;
    }

    public void put(String userId, List<SpotifyPlaylist> playlists) {
        put(userId, playlists, DEFAULT_TTL_SECONDS);
    }

    public void put(String userId, List<SpotifyPlaylist> playlists, long ttlSeconds) {
        if (userId != null && playlists != null) {
            log.info("Caching {} Spotify playlists for user {} for {} seconds", playlists.size(), userId, ttlSeconds);
            cache.put(userId, new CacheEntry(playlists, ttlSeconds));
        }
    }

    public void evict(String userId) {
        if (userId != null) {
            log.info("Evicting Spotify playlist cache for user {}", userId);
            cache.remove(userId);
        }
    }

    public void clearAll() {
        cache.clear();
    }
}
