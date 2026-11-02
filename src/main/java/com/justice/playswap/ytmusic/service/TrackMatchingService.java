package com.justice.playswap.ytmusic.service;

import com.justice.playswap.common.matching.TrackMatchScoring;
import com.justice.playswap.ytmusic.client.InnerTubeClient;
import com.justice.playswap.ytmusic.model.TrackAnalysisRequest;
import com.justice.playswap.ytmusic.model.TrackAnalysisResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
@Service("ytmusicTrackMatchingService")
@RequiredArgsConstructor
public class TrackMatchingService {

    private static final String YOUTUBE_API_BASE = "https://www.googleapis.com/youtube/v3";
    private static final int INNER_TUBE_MINIMUM_SCORE = 80;
    private static final int OFFICIAL_API_MINIMUM_SCORE = 80;
    private static final double MINIMUM_TITLE_SIMILARITY = 0.72;
    private static final double MINIMUM_ARTIST_OVERLAP = 0.75;

    private final InnerTubeClient innerTubeClient;
    private final WebClient.Builder webClientBuilder;

    public Mono<TrackAnalysisResult> analyzeTrack(TrackAnalysisRequest request, String accessToken) {
        String title = request.getTitle();
        String artist = sourceArtist(request);
        if (TrackMatchScoring.tokens(title).isEmpty()
                || TrackMatchScoring.tokens(artist).isEmpty()) {
            return Mono.just(buildFailedResult(request, "MISSING_SOURCE_METADATA"));
        }

        String searchQuery = (title + " " + artist).trim();
        log.info("Analyzing track '{}' - Spotify ID: {}", title, request.getSpotifyTrackId());

        return searchInnerTubeTier("Tier 2 (YT Music Song)", request, searchQuery,
                        innerTubeClient::searchSongs)
                .flatMap(tier2 -> {
                    if (tier2.result().isMatched()) {
                        log.info("Tier 2 (YT Music Song) match found for '{}': {}",
                                title, tier2.result().getYoutubeVideoId());
                        return Mono.just(tier2.result());
                    }

                    return searchInnerTubeTier("Tier 3 (YT Music Video)", request, searchQuery,
                                    innerTubeClient::searchVideos)
                            .flatMap(tier3 -> {
                                if (tier3.result().isMatched()) {
                                    log.info("Tier 3 (YT Music Video) match found for '{}': {}",
                                            title, tier3.result().getYoutubeVideoId());
                                    return Mono.just(tier3.result());
                                }

                                if (accessToken != null && !accessToken.isEmpty()) {
                                    return searchOfficialYoutubeApi(searchQuery, accessToken)
                                            .map(candidates -> {
                                                TrackAnalysisResult result = resolveBestMatch(
                                                        request, candidates, OFFICIAL_API_MINIMUM_SCORE);
                                                if (result.isMatched()) {
                                                    log.info("Tier 4 (Official YouTube API) match found for '{}': {}",
                                                            title, result.getYoutubeVideoId());
                                                } else if (tier2.failed() || tier3.failed()) {
                                                    return buildFailedResult(request, "ANALYSIS_ERROR");
                                                } else {
                                                    return preferSpecificFailure(tier2.result(), tier3.result(), result);
                                                }
                                                return result;
                                            });
                                }

                                log.warn("Skipping Tier 4 (Official YouTube API) for '{}' because no YouTube access token is available",
                                        title);
                                if (tier2.failed() || tier3.failed()) {
                                    return Mono.just(buildFailedResult(request, "ANALYSIS_ERROR"));
                                }
                                return Mono.just(preferSpecificFailure(
                                        tier2.result(), tier3.result(), tier3.result()));
                            });
                })
                .onErrorResume(error -> {
                    log.warn("Error during track analysis for '{}': {}", title, error.getMessage());
                    if (error instanceof WebClientResponseException webClientError
                            && webClientError.getStatusCode().value() == 401) {
                        return Mono.error(error);
                    }
                    return Mono.just(buildFailedResult(request, "ANALYSIS_ERROR"));
                });
    }

    private Mono<TierResult> searchInnerTubeTier(
            String tier,
            TrackAnalysisRequest request,
            String searchQuery,
            Function<String, Mono<List<Map<String, String>>>> search) {
        return searchInnerTubeQuery(tier, searchQuery, () -> search.apply(searchQuery))
                .flatMap(primary -> {
                    TrackAnalysisResult primaryResult = resolveBestMatch(
                            request, primary.candidates(), INNER_TUBE_MINIMUM_SCORE);
                    if (primaryResult.isMatched() || searchQuery.equals(request.getTitle().trim())) {
                        return Mono.just(new TierResult(primaryResult, primary.failed()));
                    }

                    String titleOnlyQuery = request.getTitle().trim();
                    return searchInnerTubeQuery(tier + " title-only fallback", titleOnlyQuery,
                                    () -> search.apply(titleOnlyQuery))
                            .map(fallback -> {
                                List<Map<String, String>> combined = new ArrayList<>(primary.candidates());
                                combined.addAll(fallback.candidates());
                                TrackAnalysisResult result = resolveBestMatch(
                                        request, combined, INNER_TUBE_MINIMUM_SCORE);
                                return new TierResult(result, primary.failed() || fallback.failed());
                            });
                });
    }

    private Mono<SearchOutcome> searchInnerTubeQuery(
            String tier,
            String query,
            Supplier<Mono<List<Map<String, String>>>> search) {
        return Mono.defer(search)
                .map(candidates -> new SearchOutcome(candidates, false))
                .defaultIfEmpty(new SearchOutcome(List.of(), false))
                .onErrorResume(error -> {
                    log.warn("{} failed for query '{}'; continuing to the next matching tier: {}",
                            tier, query, error.toString());
                    return Mono.just(new SearchOutcome(List.of(), true));
                });
    }

    private Mono<List<Map<String, String>>> searchOfficialYoutubeApi(String query, String accessToken) {
        var uri = UriComponentsBuilder.fromUriString(YOUTUBE_API_BASE + "/search")
                .queryParam("part", "snippet")
                .queryParam("type", "video")
                .queryParam("maxResults", 5)
                .queryParam("q", query)
                .build()
                .encode()
                .toUri();
        return webClientBuilder.build()
                .get()
                .uri(uri)
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
                        if (item == null) {
                            continue;
                        }
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
                                        "channel", channelTitle == null ? "" : channelTitle
                                ));
                            }
                        }
                    }
                    return candidates;
                });
    }

    private TrackAnalysisResult resolveBestMatch(
            TrackAnalysisRequest request,
            List<Map<String, String>> candidates,
            int minimumScore) {
        if (candidates == null || candidates.isEmpty()) {
            return buildFailedResult(request, "NO_CANDIDATES");
        }

        String targetTitle = request.getTitle();
        String targetArtist = sourceArtist(request);
        List<ScoredCandidate> scored = new ArrayList<>();
        Set<String> seenVideoIds = new HashSet<>();

        for (Map<String, String> candidate : candidates) {
            if (candidate == null) {
                continue;
            }
            String videoId = candidate.get("videoId");
            String candidateTitle = candidate.get("title");
            String candidateArtist = candidate.get("channel");
            if (videoId == null || !videoId.matches("[A-Za-z0-9_-]{11}")
                    || !seenVideoIds.add(videoId)
                    || TrackMatchScoring.tokens(candidateTitle).isEmpty()) {
                continue;
            }

            double titleSimilarity = TrackMatchScoring.titleSimilarity(
                    targetTitle, candidateTitle, targetArtist);
            if (titleSimilarity < MINIMUM_TITLE_SIMILARITY) {
                continue;
            }
            Set<String> candidateTitleExtras = new HashSet<>(TrackMatchScoring.tokens(candidateTitle));
            candidateTitleExtras.removeAll(TrackMatchScoring.tokens(targetTitle));
            double artistOverlap = TrackMatchScoring.artistOverlap(
                    targetArtist, (candidateArtist == null ? "" : candidateArtist)
                            + " " + String.join(" ", candidateTitleExtras));
            if (artistOverlap < MINIMUM_ARTIST_OVERLAP) {
                continue;
            }

            int score = (int) Math.round(titleSimilarity * 70 + artistOverlap * 30);
            scored.add(new ScoredCandidate(candidate, score));
        }

        scored.sort(Comparator.comparingInt(ScoredCandidate::score).reversed());
        if (scored.isEmpty()) {
            return buildFailedResult(request, "LOW_CONFIDENCE");
        }

        ScoredCandidate best = scored.get(0);
        if (best.score() < minimumScore) {
            return buildFailedResult(request, "LOW_CONFIDENCE");
        }
        if (scored.size() > 1 && best.score() - scored.get(1).score() < 5) {
            return buildFailedResult(request, "AMBIGUOUS_MATCH");
        }

        return TrackAnalysisResult.builder()
                .spotifyTrackId(request.getSpotifyTrackId())
                .title(request.getTitle())
                .artist(request.getArtist())
                .matched(true)
                .youtubeVideoId(best.candidate().get("videoId"))
                .confidence(best.score())
                .failureReason(null)
                .build();
    }

    private String sourceArtist(TrackAnalysisRequest request) {
        return request.getPrimaryArtist() != null && !request.getPrimaryArtist().isBlank()
                ? request.getPrimaryArtist()
                : request.getArtist();
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

    private TrackAnalysisResult preferSpecificFailure(
            TrackAnalysisResult tier2Result,
            TrackAnalysisResult tier3Result,
            TrackAnalysisResult fallback) {
        if ("AMBIGUOUS_MATCH".equals(tier2Result.getFailureReason())) {
            return tier2Result;
        }
        if ("AMBIGUOUS_MATCH".equals(tier3Result.getFailureReason())) {
            return tier3Result;
        }
        if (!"NO_CANDIDATES".equals(tier3Result.getFailureReason())) {
            return tier3Result;
        }
        if (!"NO_CANDIDATES".equals(tier2Result.getFailureReason())) {
            return tier2Result;
        }
        return fallback;
    }

    private record SearchOutcome(List<Map<String, String>> candidates, boolean failed) {
    }

    private record TierResult(TrackAnalysisResult result, boolean failed) {
    }

    private record ScoredCandidate(Map<String, String> candidate, int score) {
    }
}
