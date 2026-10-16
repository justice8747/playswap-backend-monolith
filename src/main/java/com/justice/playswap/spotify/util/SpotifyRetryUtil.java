package com.justice.playswap.spotify.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
public class SpotifyRetryUtil {

    /**
     * Exponential backoff retry specification targeting HTTP 429 (Too Many Requests) and transient 5xx errors.
     * Inspects Spotify's 'Retry-After' header if present.
     */
    public static Retry rateLimitRetrySpec() {
        return Retry.backoff(5, Duration.ofSeconds(2))
                .maxBackoff(Duration.ofSeconds(15))
                .jitter(0.5)
                .filter(throwable -> {
                    if (throwable instanceof WebClientResponseException.TooManyRequests ex) {
                        String retryAfterHeader = ex.getHeaders().getFirst("Retry-After");
                        log.warn("==> [Spotify Rate Limit] HTTP 429 Too Many Requests encountered! Retry-After header: '{}'s", retryAfterHeader);
                        return true;
                    }
                    if (throwable instanceof WebClientResponseException ex) {
                        return ex.getStatusCode().is5xxServerError();
                    }
                    return false;
                })
                .doBeforeRetry(retrySignal -> {
                    long attempt = retrySignal.totalRetries() + 1;
                    log.info("==> [Spotify Rate Limit Retry] Executing retry attempt #{} due to 429 rate limit / transient error...", attempt);
                });
    }
}
