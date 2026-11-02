package com.justice.playswap.spotify;

import com.justice.playswap.spotify.model.TrackAnalysisRequest;
import com.justice.playswap.spotify.model.TrackAnalysisResult;
import com.justice.playswap.spotify.service.TrackMatchingService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrackMatchingServiceTest {

    @Test
    void acceptsExactIsrcAndUsesSpotifyCandidateId() {
        String response = """
                {"tracks":{"items":[{
                  "id":"spotify-target-id",
                  "name":"Song Name",
                  "artists":[{"name":"Artist Name"}],
                  "album":{"name":"Album Name"},
                  "duration_ms":201000,
                  "external_ids":{"isrc":"US-ABC-12-00001"}
                }]}}
                """;
        TrackAnalysisRequest request = TrackAnalysisRequest.builder()
                .title("Song Name")
                .artist("Artist Name")
                .primaryArtist("Artist Name")
                .album("Album Name")
                .durationMs(200_000)
                .isrc("USABC1200001")
                .build();

        StepVerifier.create(service(response).analyzeTrack(request, "token"))
                .assertNext(result -> {
                    assertTrue(result.isMatched());
                    assertEquals("spotify-target-id", result.getSpotifyTrackId());
                    assertEquals(100, result.getConfidence());
                })
                .verifyComplete();
    }

    @Test
    void rejectsExactTitleWhenArtistEvidenceDoesNotMatch() {
        String response = """
                {"tracks":{"items":[{
                  "id":"wrong-artist-track",
                  "name":"Song Name",
                  "artists":[{"name":"Different Artist"}],
                  "album":{"name":"Album Name"},
                  "duration_ms":200000,
                  "external_ids":{"isrc":"GBXYZ12000001"}
                }]}}
                """;
        TrackAnalysisRequest request = TrackAnalysisRequest.builder()
                .title("Song Name")
                .artist("Expected Artist")
                .primaryArtist("Expected Artist")
                .album("Album Name")
                .durationMs(200_000)
                .build();

        StepVerifier.create(service(response).analyzeTrack(request, "token"))
                .assertNext(result -> {
                    assertFalse(result.isMatched());
                    assertEquals("LOW_CONFIDENCE", result.getFailureReason());
                })
                .verifyComplete();
    }

    @Test
    void rejectsCandidatesWithConflictingIsrcOrDuration() {
        String response = """
                {"tracks":{"items":[{
                  "id":"different-recording",
                  "name":"Song Name",
                  "artists":[{"name":"Artist Name"}],
                  "album":{"name":"Album Name"},
                  "duration_ms":240000,
                  "external_ids":{"isrc":"GBXYZ12000001"}
                }]}}
                """;
        TrackAnalysisRequest request = TrackAnalysisRequest.builder()
                .title("Song Name")
                .artist("Artist Name")
                .primaryArtist("Artist Name")
                .durationMs(200_000)
                .isrc("USABC1200001")
                .build();

        StepVerifier.create(service(response).analyzeTrack(request, "token"))
                .assertNext(result -> {
                    assertFalse(result.isMatched());
                    assertEquals("LOW_CONFIDENCE", result.getFailureReason());
                })
                .verifyComplete();
    }

    @Test
    void rejectsMissingTitleWhenNoExactIsrcCanIdentifyTheTrack() {
        TrackAnalysisRequest request = TrackAnalysisRequest.builder()
                .title("  ")
                .artist("Artist Name")
                .build();

        StepVerifier.create(service("{\"tracks\":{\"items\":[]}}").analyzeTrack(request, "token"))
                .assertNext(result -> {
                    assertFalse(result.isMatched());
                    assertEquals("MISSING_SOURCE_METADATA", result.getFailureReason());
                })
                .verifyComplete();
    }

    @Test
    void fallsBackFromIsrcSearchToTitleAndArtistSearch() {
        String matchingResponse = """
                {"tracks":{"items":[{
                  "id":"fallback-track-id",
                  "name":"Song Name",
                  "artists":[{"name":"Artist Name"}],
                  "album":{"name":"Album Name"},
                  "duration_ms":200000,
                  "external_ids":{"isrc":"USABC1200001"}
                }]}}
                """;
        ExchangeFunction exchange = request -> {
            String body = request.url().getRawQuery().contains("isrc")
                    ? "{\"tracks\":{\"items\":[]}}"
                    : matchingResponse;
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .body(body)
                    .build());
        };
        TrackAnalysisRequest request = TrackAnalysisRequest.builder()
                .title("Song Name")
                .artist("Artist Name")
                .primaryArtist("Artist Name")
                .isrc("USABC1200001")
                .build();

        StepVerifier.create(new TrackMatchingService(WebClient.builder().exchangeFunction(exchange))
                        .analyzeTrack(request, "token"))
                .assertNext(result -> {
                    assertTrue(result.isMatched());
                    assertEquals("fallback-track-id", result.getSpotifyTrackId());
                })
                .verifyComplete();
    }

    private TrackMatchingService service(String response) {
        ExchangeFunction exchange = request -> Mono.just(ClientResponse.create(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(response)
                .build());
        return new TrackMatchingService(WebClient.builder().exchangeFunction(exchange));
    }
}
