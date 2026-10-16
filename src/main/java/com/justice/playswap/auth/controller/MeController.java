package com.justice.playswap.auth.controller;

import com.justice.playswap.auth.entity.User;
import com.justice.playswap.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class MeController {

    private final UserRepository userRepository;

    @GetMapping("/spotify")
    public Mono<ResponseEntity<Map<String, Object>>> getSpotifyProfile(
            @AuthenticationPrincipal Jwt jwt) {

        return Mono.fromCallable(() -> {
            String preferredUsername = jwt.getClaimAsString("preferred_username");
            String userId = preferredUsername != null ? preferredUsername : jwt.getSubject();
            User user = userRepository.findById(UUID.fromString(userId))
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Map<String, Object> response = new HashMap<>();

            if (user.getSpotifyId() == null) {
                response.put("connected", false);
            } else {
                response.put("connected", true);
                response.put("spotifyId", user.getSpotifyId());
                String spotifyEmail = user.getSpotifyEmail();
                if (spotifyEmail == null) {
                    spotifyEmail = user.getEmail();
                }
                response.put("email", spotifyEmail != null ? spotifyEmail : "");
            }

            return ResponseEntity.ok(response);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/ytmusic")
    public Mono<ResponseEntity<Map<String, Object>>> getYoutubeProfile(
            @AuthenticationPrincipal Jwt jwt) {

        return Mono.fromCallable(() -> {
            String preferredUsername = jwt.getClaimAsString("preferred_username");
            String userId = preferredUsername != null ? preferredUsername : jwt.getSubject();
            User user = userRepository.findById(UUID.fromString(userId))
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Map<String, Object> response = new HashMap<>();

            if (user.getYoutubeId() == null) {
                response.put("connected", false);
            } else {
                response.put("connected", true);
                response.put("youtubeId", user.getYoutubeId());
                String ytEmail = user.getYoutubeEmail();
                if (ytEmail == null) {
                    ytEmail = user.getEmail();
                }
                response.put("email", ytEmail != null ? ytEmail : "");
            }

            return ResponseEntity.ok(response);
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
