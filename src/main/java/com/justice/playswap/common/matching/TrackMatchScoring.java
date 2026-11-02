package com.justice.playswap.common.matching;

import java.text.Normalizer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public final class TrackMatchScoring {

    private static final Set<String> TITLE_DECORATION = Set.of(
            "official", "audio", "video", "lyrics", "lyric", "visualizer", "hd", "4k", "vevo", "topic"
    );
    private static final Set<String> ARTIST_FILLER = Set.of("the", "a", "an", "and", "feat", "ft", "featuring");

    private TrackMatchScoring() {
    }

    public static Set<String> tokens(String value) {
        if (value == null || value.isBlank()) {
            return Set.of();
        }

        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", " ")
                .trim();
        if (normalized.isEmpty()) {
            return Set.of();
        }

        return Arrays.stream(normalized.split("\\s+"))
                .filter(token -> !token.isBlank() && !TITLE_DECORATION.contains(token))
                .collect(Collectors.toUnmodifiableSet());
    }

    public static double titleSimilarity(String expected, String candidate) {
        Set<String> expectedTokens = tokens(expected);
        Set<String> candidateTokens = tokens(candidate);
        if (expectedTokens.isEmpty() || candidateTokens.isEmpty()) {
            return 0;
        }

        long overlap = expectedTokens.stream().filter(candidateTokens::contains).count();
        return (2.0 * overlap) / (expectedTokens.size() + candidateTokens.size());
    }

    public static double titleSimilarity(String expected, String candidate, String candidateExtraTerms) {
        Set<String> expectedTokens = new HashSet<>(tokens(expected));
        Set<String> candidateTokens = new HashSet<>(tokens(candidate));
        candidateTokens.removeAll(tokens(candidateExtraTerms));
        if (expectedTokens.isEmpty() || candidateTokens.isEmpty()) {
            return 0;
        }

        long overlap = expectedTokens.stream().filter(candidateTokens::contains).count();
        double adjustedSimilarity = (2.0 * overlap) / (expectedTokens.size() + candidateTokens.size());
        return Math.max(adjustedSimilarity, titleSimilarity(expected, candidate));
    }

    public static double artistOverlap(String expected, String candidate) {
        Set<String> expectedTokens = new HashSet<>(tokens(expected));
        Set<String> candidateTokens = new HashSet<>(tokens(candidate));
        expectedTokens.removeAll(ARTIST_FILLER);
        candidateTokens.removeAll(ARTIST_FILLER);
        if (expectedTokens.isEmpty() || candidateTokens.isEmpty()) {
            return 0;
        }

        long overlap = expectedTokens.stream().filter(candidateTokens::contains).count();
        return (double) overlap / expectedTokens.size();
    }

    public static String normalizeIsrc(String value) {
        if (value == null) {
            return "";
        }
        return value.replaceAll("[^A-Za-z0-9]", "").toUpperCase(Locale.ROOT);
    }
}
