package com.justice.playswap.auth.service;

import com.justice.playswap.auth.entity.AccessToken;
import com.justice.playswap.auth.repository.AccessTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Instant;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenRefreshService {

    private final AccessTokenRepository accessTokenRepository;
    private final WebClient.Builder webClientBuilder;

    @Value("${spring.security.oauth2.client.registration.spotify.client-id}")
    private String spotifyClientId;

    @Value("${spring.security.oauth2.client.registration.spotify.client-secret}")
    private String spotifyClientSecret;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String googleClientSecret;

    // ==========================================
    // Main refresh entry point
    // ==========================================
    public Mono<AccessToken> refresh(AccessToken tokenRecord, String platform) {
        if (platform.equals("spotify")) {
            return refreshSpotifyToken(tokenRecord);
        } else if (platform.equals("youtube")) {
            return refreshYoutubeToken(tokenRecord);
        } else {
            return Mono.error(new RuntimeException(
                    "Unsupported platform: " + platform));
        }
    }

    // ==========================================
    // Spotify token refresh
    // ==========================================
    private Mono<AccessToken> refreshSpotifyToken(AccessToken tokenRecord) {
        if (tokenRecord.getRefreshToken() == null) {
            return Mono.error(new RuntimeException(
                    "No refresh token available for Spotify"));
        }

        String authHeader = "Basic " + java.util.Base64.getEncoder()
                .encodeToString((spotifyClientId + ":" + spotifyClientSecret).getBytes(java.nio.charset.StandardCharsets.UTF_8));

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "refresh_token");
        formData.add("refresh_token", tokenRecord.getRefreshToken());
        formData.add("client_id", spotifyClientId);
        formData.add("client_secret", spotifyClientSecret);

        return webClientBuilder.build()
                .post()
                .uri("https://accounts.spotify.com/api/token")
                .header("Authorization", authHeader)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(response -> {
                    String newAccessToken = (String) response.get("access_token");
                    Integer expiresIn = (Integer) response.get("expires_in");

                    // Spotify may return a new refresh token — rotate if present
                    String newRefreshToken = response.containsKey("refresh_token")
                            ? (String) response.get("refresh_token")
                            : tokenRecord.getRefreshToken();

                    tokenRecord.setAccessToken(newAccessToken);
                    tokenRecord.setRefreshToken(newRefreshToken);
                    tokenRecord.setExpiresAt(
                            Instant.now().plusSeconds(expiresIn));

                    return Mono.fromCallable(() ->
                                    accessTokenRepository.save(tokenRecord))
                            .subscribeOn(Schedulers.boundedElastic());
                })
                .doOnSuccess(t -> log.info("Spotify token refreshed for user {}",
                        tokenRecord.getUserId()))
                .doOnError(e -> log.error("Spotify token refresh failed: {}",
                        e.getMessage()));
    }

    // ==========================================
    // YouTube token refresh
    // ==========================================
    private Mono<AccessToken> refreshYoutubeToken(AccessToken tokenRecord) {
        if (tokenRecord.getRefreshToken() == null) {
            return Mono.error(new RuntimeException(
                    "No refresh token available for YouTube"));
        }

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "refresh_token");
        formData.add("refresh_token", tokenRecord.getRefreshToken());
        formData.add("client_id", googleClientId);
        formData.add("client_secret", googleClientSecret);

        return webClientBuilder.build()
                .post()
                .uri("https://oauth2.googleapis.com/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(Map.class)
                .flatMap(response -> {
                    String newAccessToken = (String) response.get("access_token");
                    Integer expiresIn = (Integer) response.get("expires_in");

                    // Google does NOT rotate refresh tokens
                    // keep the existing one
                    tokenRecord.setAccessToken(newAccessToken);
                    tokenRecord.setExpiresAt(
                             Instant.now().plusSeconds(expiresIn));

                    return Mono.fromCallable(() ->
                                    accessTokenRepository.save(tokenRecord))
                            .subscribeOn(Schedulers.boundedElastic());
                })
                .doOnSuccess(t -> log.info("YouTube token refreshed for user {}",
                        tokenRecord.getUserId()))
                .doOnError(e -> log.error("YouTube token refresh failed: {}",
                        e.getMessage()));
    }
}
