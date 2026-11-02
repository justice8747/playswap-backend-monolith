package com.justice.playswap.spotify.service;

import com.justice.playswap.common.matching.TrackMatchScoring;
import com.justice.playswap.spotify.model.TrackAnalysisRequest;
import com.justice.playswap.spotify.model.TrackAnalysisResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Slf4j
@Service("spotifyTrackMatchingService")
@RequiredArgsConstructor
public class TrackMatchingService {

    private static final String SPOTIFY_API_BASE = "https://api.spotify.com/v1";
    private static final int MINIMUM_MATCH_SCORE = 78;
    private static final double MINIMUM_TITLE_SIMILARITY = 0.70;
    private static final double MINIMUM_ARTIST_OVERLAP = 0.75;
    private static final long MAXIMUM_DURATION_DIFFERENCE_MS = 10_000;

    private final WebClient.Builder webClientBuilder;

    public Mono<TrackAnalysisResult> analyzeTrack(TrackAnalysisRequest request, String accessToken) {
        String title = request.getTitle();
        String artist = sourceArtist(request);
        String sourceIsrc = validIsrc(request.getIsrc()) ? TrackMatchScoring.normalizeIsrc(request.getIsrc()) : "";
        if ((title == null || TrackMatchScoring.tokens(title).isEmpty()) && sourceIsrc.isEmpty()) {
            return Mono.just(buildFailedResult(request, "MISSING_SOURCE_METADATA"));
        }

        String titleQuery = ((title == null ? "" : title) + " " + (artist == null ? "" : artist)).trim();
        String primaryQuery = sourceIsrc.isEmpty() ? titleQuery : "isrc:" + sourceIsrc;
        log.info("Analyzing track '{}' by '{}' on Spotify", title, artist);

        return searchSpotify(primaryQuery, accessToken)
                .flatMap(primaryCandidates -> {
                    TrackAnalysisResult primaryResult = resolveBestMatch(request, primaryCandidates);
                    if (primaryResult.isMatched() || primaryQuery.equals(titleQuery) || titleQuery.isBlank()) {
                        return Mono.just(primaryResult);
                    }

                    return searchSpotify(titleQuery, accessToken)
                            .map(fallbackCandidates -> {
                                List<SpotifyCandidate> combined = new ArrayList<>(primaryCandidates);
                                combined.addAll(fallbackCandidates);
                                return resolveBestMatch(request, combined);
                            });
                })
                .onErrorResume(error -> {
                    log.warn("Error during Spotify track analysis for '{}': {}", title, error.getMessage());
                    if (error instanceof WebClientResponseException webClientError
                            && webClientError.getStatusCode().value() == 401) {
                        return Mono.error(error);
                    }
                    return Mono.just(buildFailedResult(request, "ANALYSIS_ERROR"));
                });
    }

    @SuppressWarnings("unchecked")
    private Mono<List<SpotifyCandidate>> searchSpotify(String query, String accessToken) {
        var uri = UriComponentsBuilder.fromUriString(SPOTIFY_API_BASE + "/search")
                .queryParam("type", "track")
                .queryParam("limit", 5)
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
                .retryWhen(com.justice.playswap.spotify.util.SpotifyRetryUtil.rateLimitRetrySpec())
                .map(response -> {
                    Object tracksValue = response == null ? null : response.get("tracks");
                    if (!(tracksValue instanceof Map<?, ?> tracksMap)
                            || !(tracksMap.get("items") instanceof List<?> items)) {
                        return List.<SpotifyCandidate>of();
                    }

                    List<SpotifyCandidate> candidates = new ArrayList<>();
                    for (Object value : items) {
                        if (!(value instanceof Map<?, ?> item)) {
                            continue;
                        }

                        String id = stringValue(item.get("id"));
                        String title = stringValue(item.get("name"));
                        String album = null;
                        if (item.get("album") instanceof Map<?, ?> albumMap) {
                            album = stringValue(albumMap.get("name"));
                        }
                        String isrc = null;
                        if (item.get("external_ids") instanceof Map<?, ?> externalIds) {
                            isrc = stringValue(externalIds.get("isrc"));
                        }
                        List<String> artists = new ArrayList<>();
                        if (item.get("artists") instanceof List<?> artistItems) {
                            for (Object artistValue : artistItems) {
                                if (artistValue instanceof Map<?, ?> artistMap) {
                                    String artist = stringValue(artistMap.get("name"));
                                    if (artist != null) {
                                        artists.add(artist);
                                    }
                                }
                            }
                        }

                        if (id != null && title != null) {
                            candidates.add(new SpotifyCandidate(
                                    id,
                                    title,
                                    String.join(" ", artists),
                                    album,
                                    numberValue(item.get("duration_ms")),
                                    isrc
                            ));
                        }
                    }
                    return candidates;
                });
    }

    private TrackAnalysisResult resolveBestMatch(TrackAnalysisRequest request, List<SpotifyCandidate> candidates) {
        if (candidates == null || candidates.isEmpty()) {
            return buildFailedResult(request, "NO_CANDIDATES");
        }

        String sourceTitle = request.getTitle();
        var sourceTitleTokens = TrackMatchScoring.tokens(sourceTitle);
        String sourceArtist = sourceArtist(request);
        String sourceIsrc = validIsrc(request.getIsrc())
                ? TrackMatchScoring.normalizeIsrc(request.getIsrc())
                : "";
        List<ScoredCandidate> scored = new ArrayList<>();

        for (SpotifyCandidate candidate : candidates) {
            if (candidate.id() == null || candidate.id().isBlank()) {
                continue;
            }

            String candidateIsrc = validIsrc(candidate.isrc())
                    ? TrackMatchScoring.normalizeIsrc(candidate.isrc())
                    : "";
            boolean exactIsrc = !sourceIsrc.isEmpty() && sourceIsrc.equals(candidateIsrc);
            if (!sourceIsrc.isEmpty() && !candidateIsrc.isEmpty() && !exactIsrc) {
                continue;
            }
            if (exactIsrc) {
                scored.add(new ScoredCandidate(candidate, 100, true));
                continue;
            }
            if (sourceTitleTokens.isEmpty() || TrackMatchScoring.tokens(candidate.title()).isEmpty()) {
                continue;
            }

            double titleSimilarity = TrackMatchScoring.titleSimilarity(
                    sourceTitle, candidate.title(), candidate.artists());
            if (titleSimilarity < MINIMUM_TITLE_SIMILARITY) {
                continue;
            }

            if (sourceArtist == null || TrackMatchScoring.tokens(sourceArtist).isEmpty()
                    || TrackMatchScoring.tokens(candidate.artists()).isEmpty()) {
                continue;
            }
            double artistOverlap = TrackMatchScoring.artistOverlap(sourceArtist, candidate.artists());
            if (artistOverlap < MINIMUM_ARTIST_OVERLAP) {
                continue;
            }

            if (request.getDurationMs() > 0 && candidate.durationMs() > 0
                    && Math.abs(request.getDurationMs() - candidate.durationMs()) > MAXIMUM_DURATION_DIFFERENCE_MS) {
                continue;
            }

            boolean candidateIsrcMissing = !sourceIsrc.isEmpty() && candidateIsrc.isEmpty();
            if (candidateIsrcMissing
                    && (titleSimilarity < 0.90
                    || artistOverlap < 0.80
                    || request.getAlbum() == null
                    || candidate.album() == null
                    || TrackMatchScoring.titleSimilarity(request.getAlbum(), candidate.album()) < 0.80
                    || request.getDurationMs() <= 0
                    || candidate.durationMs() <= 0
                    || Math.abs(request.getDurationMs() - candidate.durationMs()) > 2_500)) {
                continue;
            }

            int score = (int) Math.round(titleSimilarity * 60 + artistOverlap * 25);
            if (request.getAlbum() != null && candidate.album() != null
                    && TrackMatchScoring.titleSimilarity(request.getAlbum(), candidate.album()) >= 0.80) {
                score += 5;
            }
            score += durationScore(request.getDurationMs(), candidate.durationMs());
            scored.add(new ScoredCandidate(candidate, Math.min(score, 100), false));
        }

        scored.sort(Comparator.comparingInt(ScoredCandidate::score).reversed());
        if (scored.isEmpty()) {
            return buildFailedResult(request,
                    sourceTitleTokens.isEmpty() && sourceIsrc.isEmpty()
                            ? "MISSING_SOURCE_METADATA"
                            : "LOW_CONFIDENCE");
        }

        ScoredCandidate best = scored.get(0);
        if (best.score() < MINIMUM_MATCH_SCORE) {
            return buildFailedResult(request, "LOW_CONFIDENCE");
        }
        if (!best.exactIsrc() && scored.size() > 1
                && !scored.get(1).candidate().id().equals(best.candidate().id())
                && best.score() - scored.get(1).score() < 5) {
            return buildFailedResult(request, "AMBIGUOUS_MATCH");
        }

        return TrackAnalysisResult.builder()
                .spotifyTrackId(best.candidate().id())
                .title(request.getTitle())
                .artist(request.getArtist())
                .matched(true)
                .confidence(best.score())
                .failureReason(null)
                .build();
    }

    private int durationScore(long sourceDurationMs, long candidateDurationMs) {
        if (sourceDurationMs <= 0 || candidateDurationMs <= 0) {
            return 0;
        }
        long difference = Math.abs(sourceDurationMs - candidateDurationMs);
        if (difference <= 2_500) {
            return 10;
        }
        if (difference <= 5_000) {
            return 7;
        }
        return 3;
    }

    private String sourceArtist(TrackAnalysisRequest request) {
        return request.getPrimaryArtist() != null && !request.getPrimaryArtist().isBlank()
                ? request.getPrimaryArtist()
                : request.getArtist();
    }

    private boolean validIsrc(String value) {
        return value != null && TrackMatchScoring.normalizeIsrc(value).matches("[A-Z]{2}[A-Z0-9]{3}[0-9]{7}");
    }

    private String stringValue(Object value) {
        return value instanceof String string && !string.isBlank() ? string : null;
    }

    private long numberValue(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        return 0;
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

    private record SpotifyCandidate(
            String id,
            String title,
            String artists,
            String album,
            long durationMs,
            String isrc) {
    }

    private record ScoredCandidate(SpotifyCandidate candidate, int score, boolean exactIsrc) {
    }
}
