package com.justice.playswap.common.provider;

import reactor.core.publisher.Mono;

public interface TokenProvider {
    Mono<String> getValidAccessToken(String userId, String platform);
}
