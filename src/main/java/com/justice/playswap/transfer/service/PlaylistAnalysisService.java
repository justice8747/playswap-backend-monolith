package com.justice.playswap.transfer.service;

import com.justice.playswap.common.provider.SpotifyOperations;
import com.justice.playswap.common.provider.YtmusicOperations;
import com.justice.playswap.transfer.dto.*;
import com.justice.playswap.transfer.model.*;
import com.justice.playswap.transfer.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaylistAnalysisService {

    private final TransferSessionRepository sessionRepository;
    private final TransferJobRepository jobRepository;
    private final TransferItemRepository itemRepository;
    private final SongMappingRepository songMappingRepository;
    private final SpotifyOperations spotifyOperations;
    private final YtmusicOperations ytmusicOperations;

    public Mono<AnalysisSummaryResponse> analyzePlaylists(String userIdStr, AnalyzePlaylistsRequest request) {
        log.info("Starting playlist analysis in-process for user {} across {} playlists", userIdStr, request.getPlaylistIds().size());

        UUID userUuid = null;
        try {
            if (userIdStr != null) {
                userUuid = UUID.fromString(userIdStr);
            }
        } catch (IllegalArgumentException e) {
            log.warn("Could not parse userId to UUID: {}", userIdStr);
        }

        // 1. Create Transfer Session in CREATED state then transition to ANALYZING
        TransferSession session = TransferSession.builder()
                .userId(userUuid)
                .sourcePlatform(request.getSourcePlatform())
                .targetPlatform(request.getTargetPlatform())
                .idempotencyKey(UUID.randomUUID().toString())
                .status(TransferStatus.ANALYZING.name())
                .build();
        session = sessionRepository.save(session);
        final UUID sessionId = session.getId();

        AtomicInteger totalTracksCount = new AtomicInteger(0);
        AtomicInteger totalMatchedCount = new AtomicInteger(0);
        AtomicInteger totalUnmatchedCount = new AtomicInteger(0);
        AtomicInteger totalCachedCount = new AtomicInteger(0);

        List<JobSummaryDto> jobSummaries = Collections.synchronizedList(new ArrayList<>());

        // 2. Process each playlist
        return Flux.fromIterable(request.getPlaylistIds())
                .concatMap(playlistId -> {
                    String playlistName = request.getPlaylistNames() != null ?
                            request.getPlaylistNames().getOrDefault(playlistId, "Playlist " + playlistId) : "Playlist " + playlistId;

                    TransferJob job = TransferJob.builder()
                            .sessionId(sessionId)
                            .sourcePlaylistId(playlistId)
                            .sourcePlaylistName(playlistName)
                            .idempotencyKey(UUID.randomUUID().toString())
                            .status(TransferStatus.ANALYZING.name())
                            .build();
                    TransferJob savedJob = jobRepository.save(job);

                    Mono<List<SpotifyTrackDto>> tracksMono;
                    String sourcePlatform = request.getSourcePlatform() != null ? request.getSourcePlatform().toUpperCase() : "";
                    if (sourcePlatform.contains("YTMUSIC") || sourcePlatform.contains("YOUTUBE")) {
                        tracksMono = ytmusicOperations.getPlaylistTracks(userIdStr, playlistId);
                    } else {
                        tracksMono = spotifyOperations.getPlaylistTracks(userIdStr, playlistId);
                    }

                    return tracksMono
                            .flatMap(tracks -> {
                                log.info("Fetched {} tracks for playlist {} from source platform {}", tracks.size(), playlistName, request.getSourcePlatform());
                                AtomicInteger jobMatched = new AtomicInteger(0);
                                AtomicInteger jobUnmatched = new AtomicInteger(0);

                                return Flux.fromIterable(tracks)
                                        .flatMap(track -> processTrack(userIdStr, savedJob.getId(), request.getSourcePlatform(), request.getTargetPlatform(), track, totalCachedCount)
                                                .doOnNext(matched -> {
                                                    totalTracksCount.incrementAndGet();
                                                    if (matched) {
                                                        jobMatched.incrementAndGet();
                                                        totalMatchedCount.incrementAndGet();
                                                    } else {
                                                        jobUnmatched.incrementAndGet();
                                                        totalUnmatchedCount.incrementAndGet();
                                                    }
                                                }), 5) // Concurrency limit 5
                                        .then(Mono.fromRunnable(() -> {
                                            savedJob.setStatus(TransferStatus.READY.name());
                                            jobRepository.save(savedJob);
                                            jobSummaries.add(JobSummaryDto.builder()
                                                    .jobId(savedJob.getId())
                                                    .playlistId(playlistId)
                                                    .playlistName(playlistName)
                                                    .totalTracks(tracks.size())
                                                    .matchedCount(jobMatched.get())
                                                    .unmatchedCount(jobUnmatched.get())
                                                    .build());
                                        }));
                            })
                            .onErrorResume(e -> {
                                log.error("Error analyzing playlist {}: {}", playlistId, e.getMessage());
                                savedJob.setStatus(TransferStatus.FAILED.name());
                                jobRepository.save(savedJob);
                                return Mono.empty();
                            });
                })
                .then(Mono.defer(() -> {
                    TransferSession finalSession = sessionRepository.findById(sessionId).orElseThrow();
                    finalSession.setStatus(TransferStatus.READY.name());
                    sessionRepository.save(finalSession);

                    return Mono.just(AnalysisSummaryResponse.builder()
                            .sessionId(sessionId)
                            .totalPlaylists(request.getPlaylistIds().size())
                            .totalTracks(totalTracksCount.get())
                            .matchedTracks(totalMatchedCount.get())
                            .unmatchedTracks(totalUnmatchedCount.get())
                            .cachedMatches(totalCachedCount.get())
                            .status(TransferStatus.READY.name())
                            .jobs(jobSummaries)
                            .build());
                }));
    }

    private Mono<Boolean> processTrack(String userId, UUID jobId, String sourcePlatform, String targetPlatform,
                                       SpotifyTrackDto track, AtomicInteger totalCachedCount) {
        return Mono.fromCallable(() -> songMappingRepository
                        .findBySourcePlatformAndSourceTrackIdAndDestinationPlatform(
                                sourcePlatform, track.getTrackId(), targetPlatform))
                .map(mapping -> new MappingLookup(mapping, false))
                .onErrorResume(error -> {
                    log.error("Failed to look up cached mapping for source track '{}': {}",
                            track.getTrackId(), error.getMessage(), error);
                    return persistFailedAnalysisItem(jobId, track,
                                    "Mapping lookup failed: " + error.getClass().getSimpleName())
                            .map(ignored -> new MappingLookup(Optional.empty(), true));
                })
                .flatMap(lookup -> {
                    if (lookup.failed()) {
                        return Mono.just(false);
                    }
                    if (lookup.mapping().isPresent()) {
                        SongMapping mapping = lookup.mapping().get();
                        if (isUsableCachedTargetId(mapping.getDestinationTrackId(), targetPlatform)) {
                            mapping.setLastUsedAt(LocalDateTime.now());
                            songMappingRepository.save(mapping);

                            TransferItem item = TransferItem.builder()
                                    .jobId(jobId)
                                    .sourceTrackId(track.getTrackId())
                                    .targetTrackId(mapping.getDestinationTrackId())
                                    .matchedTrackUri(mapping.getDestinationTrackId())
                                    .status(ItemStatus.PENDING.name())
                                    .confidenceScore(mapping.getConfidenceScore())
                                    .retryCount(0)
                                    .build();
                            itemRepository.save(item);
                            totalCachedCount.incrementAndGet();
                            return Mono.just(true);
                        }

                        log.warn("Ignoring invalid cached destination track ID '{}' for platform '{}' and source track '{}'",
                                mapping.getDestinationTrackId(), targetPlatform, track.getTrackId());
                        songMappingRepository.delete(mapping);
                    }

                    return analyzeAndPersistTrack(userId, jobId, sourcePlatform, targetPlatform, track);
                });
    }

    private Mono<Boolean> analyzeAndPersistTrack(
            String userId,
            UUID jobId,
            String sourcePlatform,
            String targetPlatform,
            SpotifyTrackDto track) {
        return Mono.defer(() -> {
                    String target = targetPlatform != null ? targetPlatform.toUpperCase(Locale.ROOT) : "";
                    return target.contains("SPOTIFY")
                            ? spotifyOperations.analyzeTrack(userId, track)
                            : ytmusicOperations.analyzeTrack(userId, track);
                })
                .map(result -> new AnalysisOutcome(result, null))
                .switchIfEmpty(Mono.just(new AnalysisOutcome(null, "Analysis provider returned no result")))
                .onErrorResume(error -> {
                    log.error("Track analysis failed for source track '{}': {}",
                            track.getTrackId(), error.getMessage(), error);
                    return Mono.just(new AnalysisOutcome(null, describeAnalysisError(error)));
                })
                .flatMap(outcome -> outcome.failureReason() != null
                        ? persistFailedAnalysisItem(jobId, track, outcome.failureReason())
                        : Mono.fromCallable(() -> persistAnalysisResult(
                                jobId, sourcePlatform, targetPlatform, track, outcome.result())));
    }

    private boolean persistAnalysisResult(
            UUID jobId,
            String sourcePlatform,
            String targetPlatform,
            SpotifyTrackDto track,
            TrackAnalysisResultDto result) {
        String matchedTargetId = result != null && result.isMatched()
                ? getMatchedTargetId(result, targetPlatform)
                : null;

        if (matchedTargetId != null && !matchedTargetId.isBlank()) {
            SongMapping newMapping = SongMapping.builder()
                    .sourcePlatform(sourcePlatform)
                    .sourceTrackId(track.getTrackId())
                    .destinationPlatform(targetPlatform)
                    .destinationTrackId(matchedTargetId)
                    .title(track.getTitle())
                    .artist(track.getArtists())
                    .confidenceScore(result.getConfidence())
                    .lastUsedAt(LocalDateTime.now())
                    .build();
            songMappingRepository.save(newMapping);

            TransferItem item = TransferItem.builder()
                    .jobId(jobId)
                    .sourceTrackId(track.getTrackId())
                    .targetTrackId(matchedTargetId)
                    .matchedTrackUri(matchedTargetId)
                    .status(ItemStatus.PENDING.name())
                    .confidenceScore(result.getConfidence())
                    .retryCount(0)
                    .build();
            itemRepository.save(item);
            return true;
        }

        String failureReason = result == null ? "ANALYSIS_ERROR" : result.getFailureReason();
        boolean invalidMatchedResult = result != null && result.isMatched();
        if (invalidMatchedResult) {
            failureReason = "MATCHED_RESULT_MISSING_DESTINATION_ID";
        } else if (failureReason == null || failureReason.isBlank()) {
            failureReason = "NO_MATCH_FOUND";
        }

        boolean analysisFailed = invalidMatchedResult || "ANALYSIS_ERROR".equalsIgnoreCase(failureReason);
        TransferItem item = TransferItem.builder()
                .jobId(jobId)
                .sourceTrackId(track.getTrackId())
                .targetTrackId(null)
                .status(analysisFailed ? ItemStatus.FAILED.name() : ItemStatus.SKIPPED.name())
                .errorMessage(failureReason)
                .confidenceScore(result == null ? 0 : result.getConfidence())
                .retryCount(0)
                .build();
        itemRepository.save(item);
        return false;
    }

    private Mono<Boolean> persistFailedAnalysisItem(UUID jobId, SpotifyTrackDto track, String failureReason) {
        TransferItem item = TransferItem.builder()
                .jobId(jobId)
                .sourceTrackId(track.getTrackId())
                .targetTrackId(null)
                .status(ItemStatus.FAILED.name())
                .errorMessage(failureReason)
                .confidenceScore(0)
                .retryCount(0)
                .build();
        return Mono.fromCallable(() -> itemRepository.save(item))
                .thenReturn(false);
    }

    private String describeAnalysisError(Throwable error) {
        if (error instanceof org.springframework.web.reactive.function.client.WebClientResponseException responseError) {
            return "Provider returned HTTP " + responseError.getStatusCode().value()
                    + " " + responseError.getStatusText();
        }
        String message = error.getMessage();
        if (message == null || message.isBlank()) {
            return "Analysis provider error: " + error.getClass().getSimpleName();
        }
        return ("Analysis provider error: " + message).substring(
                0, Math.min(("Analysis provider error: " + message).length(), 500));
    }

    private record MappingLookup(Optional<SongMapping> mapping, boolean failed) {
    }

    private record AnalysisOutcome(TrackAnalysisResultDto result, String failureReason) {
    }

    private String getMatchedTargetId(TrackAnalysisResultDto result, String targetPlatform) {
        String normalizedTarget = targetPlatform == null ? "" : targetPlatform.toUpperCase(Locale.ROOT);
        if (normalizedTarget.contains("SPOTIFY")) {
            return result.getSpotifyTrackId();
        }
        if (normalizedTarget.contains("YOUTUBE") || normalizedTarget.contains("YTMUSIC")) {
            String videoId = result.getYoutubeVideoId();
            if (isYoutubeVideoId(videoId)) {
                return videoId;
            }
            log.warn("Matched YouTube result for source track did not contain a valid video ID: '{}'",
                    result.getSpotifyTrackId());
            return null;
        }
        log.warn("Cannot select a destination track ID for unsupported target platform '{}'", targetPlatform);
        return null;
    }

    private boolean isUsableCachedTargetId(String destinationTrackId, String targetPlatform) {
        String normalizedTarget = targetPlatform == null ? "" : targetPlatform.toUpperCase(Locale.ROOT);
        if (normalizedTarget.contains("YOUTUBE") || normalizedTarget.contains("YTMUSIC")) {
            return isYoutubeVideoId(destinationTrackId);
        }
        return destinationTrackId != null && !destinationTrackId.isBlank();
    }

    private boolean isYoutubeVideoId(String videoId) {
        return videoId != null && videoId.matches("[A-Za-z0-9_-]{11}");
    }
}
