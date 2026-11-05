package com.justice.playswap.common.provider;

import reactor.core.publisher.Mono;

import java.util.function.Function;

public interface TokenProvider {
    Mono<String> getValidAccessToken(String userId, String platform);

    <T> Mono<T> withValidAccessToken(
            String userId,
            String platform,
            Function<String, Mono<T>> operation);

    <T> Mono<T> withValidAccessTokenWithoutRetry(
            String userId,
            String platform,
            Function<String, Mono<T>> operation);
}
