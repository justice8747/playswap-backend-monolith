package com.justice.playswap.auth.security;


import com.justice.playswap.auth.entity.User;
import com.justice.playswap.auth.service.KeycloakService;
import com.justice.playswap.auth.service.UserService;
import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpCookie;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import java.time.Instant;

import java.net.URI;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler
        implements ServerAuthenticationSuccessHandler {

    private final UserService userService;
    private final KeycloakService keycloakService;
    private final ReactiveOAuth2AuthorizedClientService authorizedClientService;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    @Override
    public Mono<Void> onAuthenticationSuccess(
            WebFilterExchange webFilterExchange,
            Authentication authentication) {

        ServerWebExchange exchange = webFilterExchange.getExchange();
        OAuth2AuthenticationToken oauthToken =
                (OAuth2AuthenticationToken) authentication;
        OAuth2User oAuth2User = oauthToken.getPrincipal();
        String registrationId = oauthToken.getAuthorizedClientRegistrationId();

        // Extract tokens from OAuth2AuthorizedClient
        return authorizedClientService.loadAuthorizedClient(
                        registrationId, oauthToken.getName())
                .flatMap(authorizedClient -> {

                    // Access token
                    String accessToken = authorizedClient
                            .getAccessToken()
                            .getTokenValue();

                    // Token expiry
                    Instant expiresAt = authorizedClient
                            .getAccessToken()
                            .getExpiresAt();

                    // Refresh token (can be null if provider didn't return one)
                    String refreshToken = null;
                    if (authorizedClient.getRefreshToken() != null) {
                        refreshToken = authorizedClient
                                .getRefreshToken()
                                .getTokenValue();
                    }

                    final String finalRefreshToken = refreshToken;
                    final String platform = registrationId.equals("google") ? "youtube" : registrationId;

                    // Extract playswap_token cookie if present
                    HttpCookie playSwapCookie = exchange.getRequest().getCookies().getFirst("playswap_token");
                    UUID existingUserIdTemp = null;

                    if (playSwapCookie != null) {
                        try {
                            String tokenValue = playSwapCookie.getValue();
                            SignedJWT signedJWT = SignedJWT.parse(tokenValue);
                            String preferredUsername = signedJWT.getJWTClaimsSet().getStringClaim("preferred_username");
                            String subject = preferredUsername != null ? preferredUsername : signedJWT.getJWTClaimsSet().getSubject();
                            existingUserIdTemp = UUID.fromString(subject);
                            log.info("Found existing user ID {} in playswap_token cookie for linking", existingUserIdTemp);
                        } catch (Exception e) {
                            log.error("Failed to parse playswap_token cookie: {}", e.getMessage());
                        }
                    }

                    final UUID existingUserId = existingUserIdTemp;

                    Mono<User> userMono;
                    if (existingUserId != null) {
                        // Door B — link account (fallback to loginOrRegister if user no longer exists in DB)
                        userMono = userService.linkAccount(existingUserId, platform, oAuth2User,
                                        accessToken, finalRefreshToken, expiresAt)
                                 .switchIfEmpty(Mono.defer(() -> {
                                     log.warn("Existing user ID {} not found in database, falling back to loginOrRegister", existingUserId);
                                     return userService.loginOrRegister(platform, oAuth2User,
                                             accessToken, finalRefreshToken, expiresAt);
                                 }));
                    } else {
                        // Door A — login or register
                        userMono = userService.loginOrRegister(platform, oAuth2User,
                                accessToken, finalRefreshToken, expiresAt);
                    }

                    return userMono
                            .flatMap(user -> keycloakService.mintToken(user.getId(), user))
                            .flatMap(jwt -> redirectWithCookie(exchange, jwt));
                });
    }

    private Mono<Void> redirectWithCookie(
            ServerWebExchange exchange, String jwt) {

        boolean isHttps = frontendUrl != null && frontendUrl.startsWith("https");

        ResponseCookie cookie = ResponseCookie.from("playswap_token", jwt)
                .httpOnly(true)
                .secure(isHttps)
                .path("/")
                .maxAge(Duration.ofDays(1))
                .sameSite(isHttps ? "None" : "Lax")
                .build();

        exchange.getResponse().addCookie(cookie);
        exchange.getResponse().getHeaders()
                .setLocation(URI.create(frontendUrl + "/connectionDashboard"));
        exchange.getResponse().setStatusCode(
                org.springframework.http.HttpStatus.FOUND);

        return exchange.getResponse().setComplete();
    }
}
