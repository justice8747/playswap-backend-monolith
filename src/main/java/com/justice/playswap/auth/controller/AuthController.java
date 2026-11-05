package com.justice.playswap.auth.controller;

import com.justice.playswap.auth.service.JwtRevocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final JwtRevocationService jwtRevocationService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @PostMapping("/logout")
    public Mono<ResponseEntity<Map<String, String>>> logout(
            ServerWebExchange exchange,
            @AuthenticationPrincipal Jwt jwt) {
        return performLogout(exchange, jwt);
    }

    private Mono<ResponseEntity<Map<String, String>>> performLogout(
            ServerWebExchange exchange,
            Jwt jwt) {

        boolean isHttps = frontendUrl != null && frontendUrl.startsWith("https");

        // Clear HTTP-only JWT auth cookie
        ResponseCookie clearCookie = ResponseCookie.from("playswap_token", "")
                .httpOnly(true)
                .secure(isHttps)
                .path("/")
                .maxAge(Duration.ZERO)
                .sameSite(isHttps ? "None" : "Lax")
                .build();
        exchange.getResponse().addCookie(clearCookie);

        return jwtRevocationService.revoke(jwt)
                .then(exchange.getSession().flatMap(WebSession::invalidate))
                .then(Mono.just(ResponseEntity.ok(Map.of("message", "Logged out successfully"))));
    }
}
