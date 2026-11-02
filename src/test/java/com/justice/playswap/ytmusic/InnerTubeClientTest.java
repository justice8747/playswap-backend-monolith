package com.justice.playswap.ytmusic;

import com.justice.playswap.ytmusic.client.InnerTubeClient;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InnerTubeClientTest {

    @Test
    void parsesMusicShelfResults() {
        String response = """
                {
                  "contents": {
                    "tabbedSearchResultsRenderer": {
                      "tabs": [{
                        "tabRenderer": {
                          "content": {
                            "sectionListRenderer": {
                              "contents": [{
                                "musicShelfRenderer": {
                                  "contents": [{
                                    "musicTwoRowItemRenderer": {
                                      "navigationEndpoint": {
                                        "watchEndpoint": {"videoId": "yt-song-123"}
                                      },
                                      "title": {"runs": [{"text": "Song Title"}]},
                                      "subtitle": {"runs": [{"text": "Artist Name"}]}
                                    }
                                  }]
                                }
                              }]
                            }
                          }
                        }
                      }]
                    }
                  }
                }
                """;

        StepVerifier.create(client(HttpStatus.OK, response).searchSongs("Song Title Artist Name"))
                .assertNext(results -> assertEquals(
                        List.of(Map.of("videoId", "yt-song-123", "title", "Song Title", "channel", "Artist Name")),
                        results))
                .verifyComplete();
    }

    @Test
    void parsesVideoRendererInTwoColumnResults() {
        String response = """
                {
                  "contents": {
                    "twoColumnSearchResultsRenderer": {
                      "primaryContents": {
                        "sectionListRenderer": {
                          "contents": [{
                            "itemSectionRenderer": {
                              "contents": [{
                                "videoRenderer": {
                                  "videoId": "YTV1d30_456",
                                  "title": {"simpleText": "Video Title"},
                                  "ownerText": {"runs": [{"text": "Artist Channel"}]}
                                }
                              }]
                            }
                          }]
                        }
                      }
                    }
                  }
                }
                """;

        StepVerifier.create(client(HttpStatus.OK, response).searchVideos("Video Title Artist"))
                .assertNext(results -> assertEquals(
                        List.of(Map.of("videoId", "YTV1d30_456", "title", "Video Title", "channel", "Artist Channel")),
                        results))
                .verifyComplete();
    }

    @Test
    void parsesResponsiveListItemVideoIdFromTitleRunNavigationEndpoint() {
        String response = """
                {
                  "contents": {
                    "tabbedSearchResultsRenderer": {
                      "tabs": [{
                        "tabRenderer": {
                          "content": {
                            "sectionListRenderer": {
                              "contents": [{
                                "musicShelfRenderer": {
                                  "contents": [{
                                    "musicResponsiveListItemRenderer": {
                                      "flexColumns": [
                                        {
                                          "musicResponsiveListItemFlexColumnRenderer": {
                                            "text": {
                                              "runs": [{
                                                "text": "Responsive Song",
                                                "navigationEndpoint": {
                                                  "watchEndpoint": {"videoId": "Yt1d3oID_01"}
                                                }
                                              }]
                                            }
                                          }
                                        },
                                        {
                                          "musicResponsiveListItemFlexColumnRenderer": {
                                            "text": {
                                              "runs": [
                                                {"text": "Artist Name"},
                                                {"text": " • "},
                                                {"text": "Album Name"}
                                              ]
                                            }
                                          }
                                        }
                                      ]
                                    }
                                  }]
                                }
                              }]
                            }
                          }
                        }
                      }]
                    }
                  }
                }
                """;

        StepVerifier.create(client(HttpStatus.OK, response).searchSongs("Responsive Song Artist Name"))
                .assertNext(results -> assertEquals(
                        List.of(Map.of(
                                "videoId", "Yt1d3oID_01",
                                "title", "Responsive Song",
                                "channel", "Artist Name")),
                        results))
                .verifyComplete();
    }

    @Test
    void reportsUnsupportedResponseStructureInsteadOfTreatingItAsNoMatch() {
        StepVerifier.create(client(HttpStatus.OK, "{\"unexpected\":{}}").searchSongs("query"))
                .expectErrorMatches(error -> error instanceof IllegalStateException
                        && error.getMessage().contains("supported search-results structure"))
                .verify();
    }

    @Test
    void propagatesHttpErrorsToMatchingTierFallback() {
        StepVerifier.create(client(HttpStatus.SERVICE_UNAVAILABLE, "{\"error\":\"unavailable\"}")
                        .searchSongs("query"))
                .expectErrorMatches(error -> error instanceof org.springframework.web.reactive.function.client.WebClientResponseException responseError
                        && responseError.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE)
                .verify();
    }

    private InnerTubeClient client(HttpStatus status, String body) {
        return new InnerTubeClient(WebClient.builder().exchangeFunction(request -> Mono.just(
                ClientResponse.create(status)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .body(body)
                        .build())));
    }
}
