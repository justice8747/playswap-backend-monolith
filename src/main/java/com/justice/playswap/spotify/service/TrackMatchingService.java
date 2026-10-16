package com.justice.playswap.spotify.service;

import com.justice.playswap.spotify.model.TrackAnalysisRequest;
import com.justice.playswap.spotify.model.TrackAnalysisResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;

@Slf4j
@Service("spotifyTrackMatchingService")
@RequiredArgsConstructor
public class TrackMatchingService {

    private final WebClient.Builder webClientBuilder;

    private static final String SPOTIFY_API_BASE = "https://api.spotify.com/v1";

    public Mono<TrackAnalysisResult> analyzeTrack(TrackAnalysisRequest request, String accessToken) {
        String artistStr = request.getPrimaryArtist() != null && !request.getPrimaryArtist().isEmpty() ?
                request.getPrimaryArtist() : request.getArtist();
        String searchQuery = (request.getTitle() + " " + (artistStr != null ? artistStr : "")).trim();
        log.info("==> [Spotify TrackMatchingService] Analyzing track '{}' by '{}' on Spotify", request.getTitle(), artistStr);

        return searchSpotify(searchQuery, accessToken)
                .map(candidates -> resolveBestMatch(request, candidates, 50))
                .onErrorResume(err -> {
                    log.warn("Error during Spotify track analysis for '{}': {}", request.getTitle(), err.getMessage());
                    return Mono.just(buildFailedResult(request, "ANALYSIS_ERROR"));
                });
    }

    @SuppressWarnings("unchecked")
    private Mono<List<Map<String, String>>> searchSpotify(String query, String accessToken) {
        String url = SPOTIFY_API_BASE + "/search?type=track&limit=5&q=" + query;

        return webClientBuilder.build()
                .get()
                .uri(url)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .retryWhen(com.justice.playswap.spotify.util.SpotifyRetryUtil.rateLimitRetrySpec())
                .map(response -> {
                    if (response == null || response.get("tracks") == null) {
                        return List.<Map<String, String>>of();
                    }
                    Map<String, Object> tracksMap = (Map<String, Object>) response.get("tracks");
                    if (tracksMap == null || tracksMap.get("items") == null) {
                        return List.<Map<String, String>>of();
                    }
                    List<Map<String, Object>> items = (List<Map<String, Object>>) tracksMap.get("items");
                    List<Map<String, String>> candidates = new ArrayList<>();

                    for (Map<String, Object> item : items) {
                        if (item == null) continue;
                        String trackId = (String) item.get("id");
                        String title = (String) item.get("name");

                        String artistName = "";
                        List<Map<String, Object>> artistsList = (List<Map<String, Object>>) item.get("artists");
                        if (artistsList != null && !artistsList.isEmpty() && artistsList.get(0) != null) {
                            artistName = (String) artistsList.get(0).get("name");
                        }

                        if (trackId != null && title != null) {
                            candidates.add(Map.of(
                                    "spotifyTrackId", trackId,
                                    "title", title,
                                    "artist", artistName != null ? artistName : ""
                            ));
                        }
                    }
                    return candidates;
                });
    }

    private TrackAnalysisResult resolveBestMatch(TrackAnalysisRequest request, List<Map<String, String>> candidates, int minThreshold) {
        if (candidates == null || candidates.isEmpty()) {
            return buildFailedResult(request, "NO_CANDIDATES");
        }

        String targetTitle = cleanText(request.getTitle());
        String targetArtist = cleanText(request.getPrimaryArtist() != null ? request.getPrimaryArtist() : request.getArtist());

        Map<String, String> bestCandidate = null;
        int maxScore = 0;

        for (Map<String, String> candidate : candidates) {
            String candTitle = cleanText(candidate.get("title"));
            String candArtist = cleanText(candidate.get("artist"));

            int score = calculateMatchScore(targetTitle, targetArtist, candTitle, candArtist);
            if (score > maxScore) {
                maxScore = score;
                bestCandidate = candidate;
            }
        }

        if (bestCandidate != null && maxScore >= minThreshold) {
            return TrackAnalysisResult.builder()
                    .spotifyTrackId(bestCandidate.get("spotifyTrackId"))
                    .title(request.getTitle())
                    .artist(request.getArtist())
                    .matched(true)
                    .confidence(maxScore)
                    .failureReason(null)
                    .build();
        }

        return buildFailedResult(request, "LOW_CONFIDENCE");
    }

    private int calculateMatchScore(String targetTitle, String targetArtist, String candTitle, String candArtist) {
        int score = 0;
        if (candTitle.contains(targetTitle) || targetTitle.contains(candTitle)) {
            score += 60;
        } else {
            Set<String> targetTokens = new HashSet<>(Arrays.asList(targetTitle.split("\\s+")));
            Set<String> candTokens = new HashSet<>(Arrays.asList(candTitle.split("\\s+")));
            targetTokens.retainAll(candTokens);
            if (!targetTokens.isEmpty()) {
                score += 40;
            }
        }

        if (!targetArtist.isEmpty() && (candTitle.contains(targetArtist) || candArtist.contains(targetArtist))) {
            score += 40;
        }

        return Math.min(score, 100);
    }

    private String cleanText(String str) {
        if (str == null) return "";
        return str.toLowerCase().replaceAll("[^a-z0-9\\s]", " ").replaceAll("\\s+", " ").trim();
    }

    private TrackAnalysisResult buildFailedResult(TrackAnalysisRequest request, String reason) {
        return TrackAnalysisResult.builder()
                .spotifyTrackId(null)
                .title(request.getTitle())
                .artist(request.getArtist())
                .matched(false)
                .confidence(0)
                .failureReason(reason)
                .build();
    }
}
