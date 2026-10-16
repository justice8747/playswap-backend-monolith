package com.justice.playswap.auth.service;

import com.justice.playswap.auth.entity.User;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class KeycloakService {

    @Value("${keycloak.base-url}")
    private String keycloakBaseUrl;

    @Value("${keycloak.realm}")
    private String realm;

    @Value("${keycloak.client-id}")
    private String clientId;

    @Value("${keycloak.client-secret}")
    private String clientSecret;

    private final WebClient.Builder webClientBuilder;
    private WebClient webClient;
    private Mono<String> adminTokenCache;

    @PostConstruct
    public void init() {
        // Build WebClient ONCE
        this.webClient = webClientBuilder.baseUrl(keycloakBaseUrl).build();
    }

    private Mono<String> getAdminToken() {
        return fetchAdminToken();
    }

    private Mono<String> fetchAdminToken() {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "client_credentials");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);

        return webClient.post()
                .uri("/realms/" + realm + "/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (String) response.get("access_token"))
                .doOnError(e -> log.error("Failed to get admin token: {}", e.getMessage()));
    }

    private Mono<String> createOrFetchKeycloakUser(String adminToken, User user) {
        String username = user.getId().toString();

        // 1. Try to fetch existing user first
        return webClient.get()
                .uri("/admin/realms/" + realm + "/users?username=" + username + "&exact=true")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .retrieve()
                .bodyToMono(List.class)
                .flatMap(list -> {
                    if (list == null || list.isEmpty()) {
                        log.info("User not found by username, creating: {}", username);
                        return createKeycloakUser(adminToken, user);
                    } else {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> keycloakUser = (Map<String, Object>) list.get(0);
                        return Mono.just((String) keycloakUser.get("id"));
                    }
                });
    }

    private Mono<String> createKeycloakUser(String adminToken, User user) {
        Map<String, Object> keycloakUser = Map.of(
                "id", user.getId().toString(),
                "username", user.getId().toString(),
                "email", user.getEmail() != null ? user.getEmail() : "",
                "enabled", true,
                "emailVerified", true
                // Note: realmRoles omitted here. You must assign roles in a separate API call.
        );

        return webClient.post()
                .uri("/admin/realms/" + realm + "/users")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(keycloakUser)
                .retrieve()
                .toBodilessEntity()
                .map(response -> {
                    URI locationUri = response.getHeaders().getLocation();
                    if (locationUri == null) {
                        throw new IllegalStateException("No Location header returned");
                    }
                    String location = locationUri.toString();
                    return location.substring(location.lastIndexOf("/") + 1);
                })
                .onErrorResume(WebClientResponseException.class, ex -> {
                    if (ex.getStatusCode() == HttpStatus.CONFLICT) {
                        // DO NOT DELETE USERS HERE.
                        // If it's a conflict but wasn't found by username, it means the EMAIL is taken.
                        return Mono.error(new RuntimeException("Email is already in use by another account"));
                    }
                    return Mono.error(ex);
                });
    }



    private Mono<String> mintJwt(String keycloakUserId) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("grant_type", "urn:ietf:params:oauth:grant-type:token-exchange");
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("requested_subject", keycloakUserId);
        formData.add("requested_token_type", "urn:ietf:params:oauth:token-type:access_token");

        return webClient.post()
                .uri("/realms/" + realm + "/protocol/openid-connect/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> (String) response.get("access_token"))
                .doOnError(e -> log.error("Failed to mint token via exchange for user {}: {}", keycloakUserId, e.getMessage()));
    }

    public Mono<String> mintToken(UUID userId, User user) {
        return getAdminToken()
                .flatMap(adminToken -> createOrFetchKeycloakUser(adminToken, user)
                        .flatMap(keycloakUserId -> mintJwt(keycloakUserId)))
                .doOnSuccess(jwt -> log.info("JWT minted for user {}", userId))
                .doOnError(e -> log.error("Failed to mint JWT for user {}: {}", userId, e.getMessage()));
    }

    public Mono<UUID> getDbUserIdFromKeycloakId(String keycloakUserId) {
        return getAdminToken().flatMap(adminToken ->
                webClient.get()
                        .uri("/admin/realms/" + realm + "/users/" + keycloakUserId)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + adminToken)
                        .retrieve()
                        .bodyToMono(Map.class)
                        .map(userMap -> {
                            String username = (String) userMap.get("username");
                            if (username == null || username.isBlank()) {
                                throw new RuntimeException("No username found for Keycloak user " + keycloakUserId);
                            }
                            return UUID.fromString(username);
                        })
                        .doOnError(e -> log.warn("Could not find Keycloak user for ID {}: {}", keycloakUserId, e.getMessage()))
        );
    }
}
