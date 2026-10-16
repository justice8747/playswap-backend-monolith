package com.justice.playswap.ytmusic.service;

import com.justice.playswap.ytmusic.client.InnerTubeClient;
import com.justice.playswap.ytmusic.model.TrackAnalysisRequest;
import com.justice.playswap.ytmusic.model.TrackAnalysisResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.*;

@Slf4j
@Service("ytmusicTrackMatchingService")
@RequiredArgsConstructor
public class TrackMatchingService {

    private final InnerTubeClient innerTubeClient;
    private final WebClient.Builder webClientBuilder;

    private static final String YOUTUBE_API_BASE = "https://www.googleapis.com/youtube/v3";

    public Mono<TrackAnalysisResult> analyzeTrack(TrackAnalysisRequest request, String accessToken) {
        String searchQuery = (request.getTitle() + " " + (request.getPrimaryArtist() != null ? request.getPrimaryArtist() : request.getArtist())).trim();
        log.info("Analyzing track '{}' - Spotify ID: {}", request.getTitle(), request.getSpotifyTrackId());

        // Tier 2: InnerTube YT Music Songs search
        return innerTubeClient.searchSongs(searchQuery)
                .flatMap(tier2Candidates -> {
                    TrackAnalysisResult result = resolveBestMatch(request, tier2Candidates, 60);
                    if (result.isMatched()) {
                        log.info("Tier 2 (YT Music Song) match found for '{}': {}", request.getTitle(), result.getYoutubeVideoId());
                        return Mono.just(result);
                    }

                    // Tier 3: InnerTube YT Music Videos search
                    return innerTubeClient.searchVideos(searchQuery)
                            .flatMap(tier3Candidates -> {
                                TrackAnalysisResult tier3Result = resolveBestMatch(request, tier3Candidates, 55);
                                if (tier3Result.isMatched()) {
                                    log.info("Tier 3 (YT Music Video) match found for '{}': {}", request.getTitle(), tier3Result.getYoutubeVideoId());
                                    return Mono.just(tier3Result);
                                }

                                // Tier 4: Official YouTube Data API search
                                if (accessToken != null && !accessToken.isEmpty()) {
                                    return searchOfficialYoutubeApi(searchQuery, accessToken)
                                            .map(tier4Candidates -> {
                                                TrackAnalysisResult tier4Result = resolveBestMatch(request, tier4Candidates, 50);
                                                if (tier4Result.isMatched()) {
                                                    log.info("Tier 4 (Official YouTube API) match found for '{}': {}", request.getTitle(), tier4Result.getYoutubeVideoId());
                                                }
                                                return tier4Result;
                                            });
                                }

                                return Mono.just(buildFailedResult(request, "NO_MATCH_FOUND"));
                            });
                })
                .onErrorResume(err -> {
                    log.warn("Error during track analysis for '{}': {}", request.getTitle(), err.getMessage());
                    return Mono.just(buildFailedResult(request, "ANALYSIS_ERROR"));
                });
    }

    private Mono<List<Map<String, String>>> searchOfficialYoutubeApi(String query, String accessToken) {
        return webClientBuilder.build()
                .get()
                .uri(YOUTUBE_API_BASE + "/search?part=snippet&type=video&maxResults=5&q=" + query)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    if (response == null || response.get("items") == null) {
                        return List.<Map<String, String>>of();
                    }
                    List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
                    List<Map<String, String>> candidates = new ArrayList<>();
                    for (Map<String, Object> item : items) {
                        Map<String, Object> idMap = (Map<String, Object>) item.get("id");
                        Map<String, Object> snippet = (Map<String, Object>) item.get("snippet");
                        if (idMap != null && snippet != null && "youtube#video".equals(idMap.get("kind"))) {
                            String videoId = (String) idMap.get("videoId");
                            String title = (String) snippet.get("title");
                            String channelTitle = (String) snippet.get("channelTitle");
                            if (videoId != null && title != null) {
                                candidates.add(Map.of(
                                        "videoId", videoId,
                                        "title", title,
                                        "channel", channelTitle != null ? channelTitle : ""
                                ));
                            }
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
            String candChannel = cleanText(candidate.get("channel"));

            int score = calculateMatchScore(targetTitle, targetArtist, candTitle, candChannel);
            if (score > maxScore) {
                maxScore = score;
                bestCandidate = candidate;
            }
        }

        if (bestCandidate != null && maxScore >= minThreshold) {
            return TrackAnalysisResult.builder()
                    .spotifyTrackId(request.getSpotifyTrackId())
                    .title(request.getTitle())
                    .artist(request.getArtist())
                    .matched(true)
                    .youtubeVideoId(bestCandidate.get("videoId"))
                    .confidence(maxScore)
                    .failureReason(null)
                    .build();
        }

        return buildFailedResult(request, "LOW_CONFIDENCE");
    }

    private int calculateMatchScore(String targetTitle, String targetArtist, String candTitle, String candChannel) {
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

        if (!targetArtist.isEmpty() && (candTitle.contains(targetArtist) || candChannel.contains(targetArtist))) {
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
                .spotifyTrackId(request.getSpotifyTrackId())
                .title(request.getTitle())
                .artist(request.getArtist())
                .matched(false)
                .youtubeVideoId(null)
                .confidence(0)
                .failureReason(reason)
                .build();
    }
}
