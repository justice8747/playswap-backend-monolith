package com.justice.playswap.ytmusic.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class InnerTubeClient {

    private final WebClient.Builder webClientBuilder;

    private static final String INNERTUBE_URL = "https://music.youtube.com/youtubei/v1/search";

    // InnerTube search params filters (decoded raw base64)
    private static final String PARAMS_SONGS = "egWKAQIYAWoKEAkQBRAKEAMQBw==";
    private static final String PARAMS_VIDEOS = "egWKAQIYAWoKEAkQChAFEAMQBw==";

    public Mono<List<Map<String, String>>> searchSongs(String query) {
        return performInnerTubeSearch(query, PARAMS_SONGS);
    }

    public Mono<List<Map<String, String>>> searchVideos(String query) {
        return performInnerTubeSearch(query, PARAMS_VIDEOS);
    }

    private Mono<List<Map<String, String>>> performInnerTubeSearch(String query, String params) {
        Map<String, Object> requestBody = Map.of(
                "context", Map.of(
                        "client", Map.of(
                                "clientName", "WEB_REMIX",
                                "clientVersion", "1.20231214.00.00",
                                "hl", "en",
                                "gl", "US"
                        )
                ),
                "query", query,
                "params", params
        );

        return webClientBuilder.build()
                .post()
                .uri(INNERTUBE_URL)
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .map(this::parseInnerTubeResponse)
                .onErrorResume(e -> {
                    log.warn("InnerTube search failed for query '{}': {}", query, e.getMessage());
                    return Mono.just(List.of());
                });
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, String>> parseInnerTubeResponse(Map<String, Object> response) {
        if (response == null) return List.of();
        List<Map<String, String>> results = new ArrayList<>();

        try {
            Map<String, Object> contents = (Map<String, Object>) response.get("contents");
            if (contents == null) return results;

            Map<String, Object> tabbed = (Map<String, Object>) contents.get("tabbedSearchResultsRenderer");
            if (tabbed == null) return results;

            List<Map<String, Object>> tabs = (List<Map<String, Object>>) tabbed.get("tabs");
            if (tabs == null || tabs.isEmpty()) return results;

            Map<String, Object> tabRenderer = (Map<String, Object>) tabs.get(0).get("tabRenderer");
            if (tabRenderer == null) return results;

            Map<String, Object> content = (Map<String, Object>) tabRenderer.get("content");
            if (content == null) return results;

            Map<String, Object> sectionList = (Map<String, Object>) content.get("sectionListRenderer");
            if (sectionList == null) return results;

            List<Map<String, Object>> sectionContents = (List<Map<String, Object>>) sectionList.get("contents");
            if (sectionContents == null || sectionContents.isEmpty()) return results;

            for (Map<String, Object> section : sectionContents) {
                Map<String, Object> shelf = (Map<String, Object>) section.get("musicShelfRenderer");
                if (shelf != null && shelf.get("contents") != null) {
                    List<Map<String, Object>> shelfItems = (List<Map<String, Object>>) shelf.get("contents");
                    for (Map<String, Object> item : shelfItems) {
                        Map<String, Object> itemRenderer = (Map<String, Object>) item.get("musicTwoRowItemRenderer");
                        if (itemRenderer == null) {
                            itemRenderer = (Map<String, Object>) item.get("musicResponsiveListItemRenderer");
                        }
                        if (itemRenderer != null) {
                            Map<String, String> extracted = extractItemData(itemRenderer);
                            if (extracted != null && extracted.containsKey("videoId")) {
                                results.add(extracted);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Error parsing InnerTube response: {}", e.getMessage());
        }

        return results;
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> extractItemData(Map<String, Object> renderer) {
        String videoId = null;
        String title = null;
        String artist = null;

        // Extract Video ID
        Map<String, Object> navEndpoint = (Map<String, Object>) renderer.get("navigationEndpoint");
        if (navEndpoint != null) {
            Map<String, Object> watchEndpoint = (Map<String, Object>) navEndpoint.get("watchEndpoint");
            if (watchEndpoint != null) {
                videoId = (String) watchEndpoint.get("videoId");
            }
        }

        // Extract Title
        Map<String, Object> titleMap = (Map<String, Object>) renderer.get("title");
        if (titleMap != null && titleMap.get("runs") instanceof List<?> runs && !runs.isEmpty()) {
            Map<String, Object> firstRun = (Map<String, Object>) runs.get(0);
            title = (String) firstRun.get("text");
        }

        // Extract Subtitle / Artist
        Map<String, Object> subtitleMap = (Map<String, Object>) renderer.get("subtitle");
        if (subtitleMap != null && subtitleMap.get("runs") instanceof List<?> runs && !runs.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (Object obj : runs) {
                Map<String, Object> run = (Map<String, Object>) obj;
                String text = (String) run.get("text");
                if (text != null && !text.isBlank() && !text.equals(" • ")) {
                    if (!sb.isEmpty()) sb.append(", ");
                    sb.append(text);
                }
            }
            artist = sb.toString();
        }

        // flexColumns (InnerTube musicResponsiveListItemRenderer)
        if (renderer.get("flexColumns") instanceof List<?> flexColumns && !flexColumns.isEmpty()) {
            Map<String, Object> col0 = (Map<String, Object>) flexColumns.get(0);
            if (col0 != null) {
                Map<String, Object> colRenderer = (Map<String, Object>) col0.get("musicResponsiveListItemFlexColumnRenderer");
                if (colRenderer != null && colRenderer.get("text") instanceof Map<?,?> textMap) {
                    if (textMap.get("runs") instanceof List<?> runs && !runs.isEmpty()) {
                        Map<String, Object> run0 = (Map<String, Object>) runs.get(0);
                        if (title == null) {
                            title = (String) run0.get("text");
                        }
                        if (videoId == null && run0.get("navigationEndpoint") instanceof Map<?,?> nav) {
                            Map<String, Object> watch = (Map<String, Object>) nav.get("watchEndpoint");
                            if (watch != null) {
                                videoId = (String) watch.get("videoId");
                            }
                        }
                    }
                }
            }

            if (flexColumns.size() > 1) {
                Map<String, Object> col1 = (Map<String, Object>) flexColumns.get(1);
                if (col1 != null) {
                    Map<String, Object> colRenderer = (Map<String, Object>) col1.get("musicResponsiveListItemFlexColumnRenderer");
                    if (colRenderer != null && colRenderer.get("text") instanceof Map<?,?> textMap) {
                        if (textMap.get("runs") instanceof List<?> runs && !runs.isEmpty()) {
                            StringBuilder sb = new StringBuilder();
                            for (Object obj : runs) {
                                Map<String, Object> run = (Map<String, Object>) obj;
                                String text = (String) run.get("text");
                                if (text != null && !text.isBlank() && !text.equals(" • ")) {
                                    if (!sb.isEmpty()) sb.append(", ");
                                    sb.append(text);
                                }
                            }
                            if (artist == null || artist.isEmpty()) {
                                artist = sb.toString();
                            }
                        }
                    }
                }
            }
        }

        // Overlay fallback for videoId
        if (videoId == null && renderer.get("overlay") instanceof Map<?,?> overlay) {
            if (overlay.get("musicItemRendererOverlayRenderer") instanceof Map<?,?> itemOverlay) {
                if (itemOverlay.get("content") instanceof Map<?,?> content) {
                    if (content.get("musicPlayButtonRenderer") instanceof Map<?,?> playBtn) {
                        if (playBtn.get("playNavigationEndpoint") instanceof Map<?,?> playNav) {
                            if (playNav.get("watchEndpoint") instanceof Map<?,?> watch) {
                                videoId = (String) watch.get("videoId");
                            }
                        }
                    }
                }
            }
        }

        if (videoId != null && title != null) {
            return Map.of(
                    "videoId", videoId,
                    "title", title,
                    "channel", artist != null ? artist : ""
            );
        }
        return null;
    }
}
