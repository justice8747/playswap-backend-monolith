package com.justice.playswap.ytmusic.client;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class InnerTubeClient {

    private static final String INNERTUBE_URL = "https://music.youtube.com/youtubei/v1/search";
    private static final String PARAMS_SONGS = "egWKAQIYAWoKEAkQBRAKEAMQBw==";
    private static final String PARAMS_VIDEOS = "egWKAQIYAWoKEAkQChAFEAMQBw==";
    private static final Set<String> SEARCH_CONTAINER_KEYS = Set.of(
            "tabbedSearchResultsRenderer",
            "twoColumnSearchResultsRenderer",
            "sectionListRenderer",
            "musicShelfRenderer",
            "musicShelfContinuation",
            "musicCardShelfRenderer",
            "musicPlaylistShelfRenderer",
            "itemSectionRenderer"
    );
    private static final Set<String> ITEM_RENDERER_KEYS = Set.of(
            "musicTwoRowItemRenderer",
            "musicResponsiveListItemRenderer",
            "videoRenderer"
    );

    private final WebClient.Builder webClientBuilder;

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
                .bodyToMono(JsonNode.class)
                .map(this::parseInnerTubeResponse)
                .doOnError(error -> log.warn("InnerTube search failed for query '{}'", query, error));
    }

    private List<Map<String, String>> parseInnerTubeResponse(JsonNode response) {
        if (response == null || !response.isObject()) {
            throw new IllegalStateException("InnerTube returned an empty or invalid JSON response");
        }

        ParseState state = new ParseState();
        collectSearchResults(response, state);

        if (!state.searchContainerFound) {
            throw new IllegalStateException("InnerTube response did not contain a supported search-results structure");
        }
        if (state.results.isEmpty() && state.invalidItemCount > 0) {
            throw new IllegalStateException("InnerTube response contained unrecognized or incomplete result items");
        }
        if (state.invalidItemCount > 0) {
            log.warn("InnerTube response contained {} unrecognized result items; continuing with {} parsed candidates",
                    state.invalidItemCount, state.results.size());
        }

        return state.results;
    }

    private void collectSearchResults(JsonNode node, ParseState state) {
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String key = field.getKey();
                JsonNode value = field.getValue();

                if (SEARCH_CONTAINER_KEYS.contains(key)) {
                    state.searchContainerFound = true;
                }
                if (ITEM_RENDERER_KEYS.contains(key)) {
                    Map<String, String> item = extractItemData(key, value);
                    if (item == null) {
                        state.invalidItemCount++;
                    } else {
                        state.results.add(item);
                    }
                }
                collectSearchResults(value, state);
            }
        } else if (node.isArray()) {
            for (JsonNode child : node) {
                collectSearchResults(child, state);
            }
        }
    }

    private Map<String, String> extractItemData(String rendererType, JsonNode renderer) {
        String videoId = firstVideoId(
                renderer.path("videoId"),
                renderer.path("playlistItemData").path("videoId"),
                renderer.path("navigationEndpoint").path("watchEndpoint").path("videoId"),
                renderer.path("overlay")
                        .path("musicItemRendererOverlayRenderer")
                        .path("content")
                        .path("musicPlayButtonRenderer")
                        .path("playNavigationEndpoint")
                        .path("watchEndpoint")
                        .path("videoId"));
        JsonNode flexColumns = renderer.path("flexColumns");
        if (videoId == null && flexColumns.isArray()) {
            videoId = findVideoId(flexColumns);
        }

        String title = text(renderer.path("title"));
        if (title == null && flexColumns.isArray() && !flexColumns.isEmpty()) {
            title = text(flexColumns.get(0)
                    .path("musicResponsiveListItemFlexColumnRenderer").path("text"));
        }
        if (title == null) {
            title = text(renderer.path("accessibilityData").path("accessibilityData").path("label"));
        }

        String artist = text(renderer.path("subtitle"));
        if (artist == null) {
            artist = firstText(renderer, "ownerText", "longBylineText", "shortBylineText");
        }
        if (artist == null && flexColumns.isArray() && flexColumns.size() > 1) {
            artist = textBeforeSeparator(flexColumns.get(1)
                    .path("musicResponsiveListItemFlexColumnRenderer").path("text"));
        }

        if (videoId == null || title == null) {
            log.debug("Could not extract a playable video ID and title from InnerTube renderer '{}'; fields: {}",
                    rendererType, fieldNames(renderer));
            return null;
        }

        Map<String, String> result = new LinkedHashMap<>();
        result.put("videoId", videoId);
        result.put("title", title);
        result.put("channel", artist == null ? "" : artist);
        return result;
    }

    private String firstVideoId(JsonNode... candidates) {
        for (JsonNode candidate : candidates) {
            String value = text(candidate);
            if (value != null && value.matches("[A-Za-z0-9_-]{11}")) {
                return value;
            }
        }
        return null;
    }

    private String findVideoId(JsonNode node) {
        if (node.isObject()) {
            JsonNode videoId = node.get("videoId");
            String value = text(videoId);
            if (value != null && value.matches("[A-Za-z0-9_-]{11}")) {
                return value;
            }
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                String nestedVideoId = findVideoId(fields.next().getValue());
                if (nestedVideoId != null) {
                    return nestedVideoId;
                }
            }
        } else if (node.isArray()) {
            for (JsonNode child : node) {
                String nestedVideoId = findVideoId(child);
                if (nestedVideoId != null) {
                    return nestedVideoId;
                }
            }
        }
        return null;
    }

    private List<String> fieldNames(JsonNode node) {
        List<String> names = new ArrayList<>();
        if (node.isObject()) {
            node.fieldNames().forEachRemaining(names::add);
        }
        return names;
    }

    private String firstText(JsonNode node, String... fields) {
        for (String field : fields) {
            String value = text(node.path(field));
            if (value != null) {
                return value;
            }
        }
        return null;
    }

    private String textBeforeSeparator(JsonNode node) {
        JsonNode runs = node.path("runs");
        if (!runs.isArray()) {
            return text(node);
        }

        List<String> parts = new ArrayList<>();
        for (JsonNode run : runs) {
            String part = text(run.path("text"));
            if (part == null) {
                continue;
            }
            if (part.equals("•") || part.equals("|")) {
                break;
            }
            parts.add(part);
        }
        return parts.isEmpty() ? null : String.join(" ", parts);
    }

    private String text(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        if (node.isTextual()) {
            String value = node.asText().trim();
            return value.isEmpty() ? null : value;
        }
        if (node.isObject()) {
            JsonNode simpleText = node.get("simpleText");
            if (simpleText != null && simpleText.isTextual()) {
                return text(simpleText);
            }
            JsonNode runs = node.get("runs");
            if (runs != null && runs.isArray()) {
                List<String> parts = new ArrayList<>();
                for (JsonNode run : runs) {
                    String part = text(run.path("text"));
                    if (part != null && !part.equals("•")) {
                        parts.add(part);
                    }
                }
                return parts.isEmpty() ? null : String.join(", ", parts);
            }
        }
        return null;
    }

    private static final class ParseState {
        private final List<Map<String, String>> results = new ArrayList<>();
        private boolean searchContainerFound;
        private int invalidItemCount;
    }
}
