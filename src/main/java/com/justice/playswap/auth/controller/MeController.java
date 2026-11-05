package com.justice.playswap.auth.controller;

import com.justice.playswap.auth.entity.User;
import com.justice.playswap.auth.repository.UserRepository;
import com.justice.playswap.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
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
    private final UserService userService;

    @GetMapping("/spotify")
    public Mono<ResponseEntity<Map<String, Object>>> getSpotifyProfile(
            @AuthenticationPrincipal Jwt jwt) {

        return Mono.fromCallable(() -> {
            UUID userId = extractUserId(jwt);
            User user = userRepository.findById(userId)
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

    @PostMapping(path = {"/spotify/disconnect"})
    @DeleteMapping(path = {"/spotify"})
    public Mono<ResponseEntity<Map<String, Object>>> disconnectSpotify(
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId = extractUserId(jwt);
        return userService.disconnectPlatform(userId, "spotify")
                .then(Mono.just(ResponseEntity.ok(Map.of(
                        "success", (Object) true,
                        "message", "Spotify account disconnected successfully",
                        "platform", "spotify"
                ))))
                .onErrorResume(IllegalStateException.class, e ->
                        Mono.just(ResponseEntity.badRequest().body(Map.of(
                                "success", false,
                                "message", e.getMessage(),
                                "platform", "spotify"
                        ))))
                .onErrorResume(Exception.class, e ->
                        Mono.just(ResponseEntity.internalServerError().body(Map.of(
                                "success", false,
                                "message", e.getMessage(),
                                "platform", "spotify"
                        ))));
    }

    @GetMapping("/ytmusic")
    public Mono<ResponseEntity<Map<String, Object>>> getYoutubeProfile(
            @AuthenticationPrincipal Jwt jwt) {

        return Mono.fromCallable(() -> {
            UUID userId = extractUserId(jwt);
            User user = userRepository.findById(userId)
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

    @PostMapping(path = {"/ytmusic/disconnect", "/youtube/disconnect"})
    @DeleteMapping(path = {"/ytmusic", "/youtube"})
    public Mono<ResponseEntity<Map<String, Object>>> disconnectYoutube(
            @AuthenticationPrincipal Jwt jwt) {

        UUID userId = extractUserId(jwt);
        return userService.disconnectPlatform(userId, "youtube")
                .then(Mono.just(ResponseEntity.ok(Map.of(
                        "success", (Object) true,
                        "message", "YouTube Music account disconnected successfully",
                        "platform", "youtube"
                ))))
                .onErrorResume(IllegalStateException.class, e ->
                        Mono.just(ResponseEntity.badRequest().body(Map.of(
                                "success", false,
                                "message", e.getMessage(),
                                "platform", "youtube"
                        ))))
                .onErrorResume(Exception.class, e ->
                        Mono.just(ResponseEntity.internalServerError().body(Map.of(
                                "success", false,
                                "message", e.getMessage(),
                                "platform", "youtube"
                        ))));
    }

    private UUID extractUserId(Jwt jwt) {
        String preferredUsername = jwt.getClaimAsString("preferred_username");
        String userIdStr = preferredUsername != null ? preferredUsername : jwt.getSubject();
        return UUID.fromString(userIdStr);
    }
}
