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
                                        .flatMap(track -> processTrack(userIdStr, savedJob.getId(), request.getSourcePlatform(), request.getTargetPlatform(), track)
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

    private Mono<Boolean> processTrack(String userId, UUID jobId, String sourcePlatform, String targetPlatform, SpotifyTrackDto track) {
        // Check existing mapping database cache (Tier 1)
        Optional<SongMapping> existingMapping = songMappingRepository
                .findBySourcePlatformAndSourceTrackIdAndDestinationPlatform(sourcePlatform, track.getTrackId(), targetPlatform);

        if (existingMapping.isPresent()) {
            SongMapping mapping = existingMapping.get();
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
            return Mono.just(true);
        }

        // Search via target module in-process
        Mono<TrackAnalysisResultDto> analysisMono;
        String target = targetPlatform != null ? targetPlatform.toUpperCase() : "";
        if (target.contains("SPOTIFY")) {
            analysisMono = spotifyOperations.analyzeTrack(userId, track);
        } else {
            analysisMono = ytmusicOperations.analyzeTrack(userId, track);
        }

        return analysisMono
                .map(result -> {
                    String matchedTargetId = null;
                    if (result != null && result.isMatched()) {
                        matchedTargetId = result.getSpotifyTrackId() != null ? result.getSpotifyTrackId() : result.getYoutubeVideoId();
                    }

                    if (matchedTargetId != null) {
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
                    } else {
                        TransferItem item = TransferItem.builder()
                                .jobId(jobId)
                                .sourceTrackId(track.getTrackId())
                                .targetTrackId(null)
                                .status(ItemStatus.SKIPPED.name())
                                .errorMessage("No match found on target platform")
                                .confidenceScore(0)
                                .retryCount(0)
                                .build();
                        itemRepository.save(item);
                        return false;
                    }
                })
                .onErrorReturn(false);
    }
}
