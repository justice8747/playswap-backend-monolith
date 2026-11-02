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
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlaylistTransferExecutionService {

    private static final int MAX_RETRIES = 3;
    private static final int MINIMUM_INSERTION_PERCENT = 90;

    private final TransferSessionRepository sessionRepository;
    private final TransferJobRepository jobRepository;
    private final TransferItemRepository itemRepository;
    private final TransferHistoryRepository transferHistoryRepository;
    private final SpotifyOperations spotifyOperations;
    private final YtmusicOperations ytmusicOperations;

    public Mono<ExecuteTransferResponse> executeTransfer(String userIdStr, ExecuteTransferRequest request) {
        log.info("Starting execution/resume of transfer in-process for session {} by user {}", request.getSessionId(), userIdStr);

        TransferSession session = sessionRepository.findById(request.getSessionId())
                .orElseThrow(() -> new IllegalArgumentException("Transfer session not found: " + request.getSessionId()));

        session.setStatus(TransferStatus.EXECUTING.name());
        sessionRepository.save(session);

        List<TransferJob> jobs = jobRepository.findBySessionId(session.getId());
        if (jobs == null || jobs.isEmpty()) {
            session.setStatus(TransferStatus.FAILED.name());
            sessionRepository.save(session);
            return Mono.error(new IllegalArgumentException("No transfer jobs found for session: " + session.getId()));
        }

        String targetPlatform = session.getTargetPlatform() != null ? session.getTargetPlatform().toUpperCase() : "YTMUSIC";
        List<TransferredPlaylistDto> transferredPlaylists = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger grandTotalTracks = new AtomicInteger(0);

        return Flux.fromIterable(jobs)
                .concatMap(job -> processOrResumeJob(userIdStr, session, job, targetPlatform, request)
                        .doOnNext(dto -> {
                            if (dto != null) {
                                transferredPlaylists.add(dto);
                                grandTotalTracks.addAndGet(dto.getTransferredTracksCount());
                            }
                        })
                )
                .then(Mono.defer(() -> {
                    // Update final session status based on jobs state
                    List<TransferJob> updatedJobs = jobRepository.findBySessionId(session.getId());
                    boolean anyFailed = updatedJobs.stream().anyMatch(j ->
                            TransferStatus.FAILED.name().equalsIgnoreCase(j.getStatus())
                                    || TransferStatus.PARTIALLY_COMPLETED.name().equalsIgnoreCase(j.getStatus()));
                    boolean allCompleted = updatedJobs.stream().allMatch(j -> TransferStatus.COMPLETED.name().equalsIgnoreCase(j.getStatus()));
                    boolean allFailed = !updatedJobs.isEmpty() && updatedJobs.stream()
                            .allMatch(j -> TransferStatus.FAILED.name().equalsIgnoreCase(j.getStatus()));

                    String finalStatus = allCompleted ? TransferStatus.COMPLETED.name()
                            : allFailed ? TransferStatus.FAILED.name()
                            : anyFailed ? TransferStatus.PARTIALLY_COMPLETED.name()
                            : TransferStatus.COMPLETED.name();
                    session.setStatus(finalStatus);
                    sessionRepository.save(session);

                    log.info("Transfer session {} completed with status: {}", session.getId(), finalStatus);
                    return Mono.just(ExecuteTransferResponse.builder()
                            .sessionId(session.getId())
                            .status(finalStatus)
                            .totalPlaylists(transferredPlaylists.size())
                            .totalTransferredTracks(grandTotalTracks.get())
                            .transferredPlaylists(transferredPlaylists)
                            .build());
                }));
    }

    private Mono<TransferredPlaylistDto> processOrResumeJob(String userIdStr, TransferSession session, TransferJob job, String targetPlatform, ExecuteTransferRequest request) {
        job.setStatus(TransferStatus.EXECUTING.name());
        jobRepository.save(job);

        List<TransferItem> allItems = itemRepository.findByJobId(job.getId());
        Set<String> matchedTrackIds = allItems.stream()
                .map(TransferItem::getTargetTrackId)
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.toSet());

        // Determine items eligible for transfer/retry
        List<TransferItem> eligibleItems = allItems.stream()
                .filter(item -> {
                    String status = item.getStatus();
                    boolean isPendingOrRetryable = ItemStatus.PENDING.name().equalsIgnoreCase(status) ||
                            ("SUCCESS".equalsIgnoreCase(status) && item.getTargetTrackId() != null) ||
                            (ItemStatus.FAILED.name().equalsIgnoreCase(status) && (item.getRetryCount() == null || item.getRetryCount() < MAX_RETRIES));
                    return isPendingOrRetryable && item.getTargetTrackId() != null && !item.getTargetTrackId().isBlank();
                })
                .collect(Collectors.toList());

        if (matchedTrackIds.isEmpty()) {
            log.warn("No eligible tracks to transfer for job {} (Playlist: {})", job.getId(), job.getSourcePlaylistName());
            job.setStatus("TRANSFERRED_EMPTY");
            jobRepository.save(job);
            return Mono.just(TransferredPlaylistDto.builder()
                    .jobId(job.getId())
                    .playlistName(job.getSourcePlaylistName() != null ? job.getSourcePlaylistName() : "Playlist")
                    .targetPlaylistId(job.getTargetPlaylistId())
                    .targetPlaylistUrl(job.getTargetPlaylistUrl())
                    .transferredTracksCount(0)
                    .build());
        }

        if (eligibleItems.isEmpty()) {
            Set<String> insertedTrackIds = getSuccessfulTrackIds(allItems);
            if (!meetsInsertionThreshold(insertedTrackIds.size(), matchedTrackIds.size())) {
                return rollbackFailedTransfer(userIdStr, job, targetPlatform, allItems,
                        job.getTargetPlaylistId(), job.getTargetPlaylistUrl(),
                        insertedTrackIds.size(), matchedTrackIds.size());
            }

            job.setStatus(insertedTrackIds.size() == matchedTrackIds.size()
                    ? TransferStatus.COMPLETED.name()
                    : TransferStatus.PARTIALLY_COMPLETED.name());
            jobRepository.save(job);
            return Mono.just(TransferredPlaylistDto.builder()
                    .jobId(job.getId())
                    .playlistName(job.getSourcePlaylistName() != null ? job.getSourcePlaylistName() : "Playlist")
                    .targetPlaylistId(job.getTargetPlaylistId())
                    .targetPlaylistUrl(job.getTargetPlaylistUrl())
                    .transferredTracksCount(insertedTrackIds.size())
                    .build());
        }

        // Idempotency: Create target playlist only if targetPlaylistId is not yet created
        Mono<CreatePlaylistResponseDto> playlistMono;
        if (job.getTargetPlaylistId() != null && !job.getTargetPlaylistId().isBlank()) {
            log.info("Idempotency match: Reusing existing target playlist {} for job {}", job.getTargetPlaylistId(), job.getId());
            playlistMono = Mono.just(CreatePlaylistResponseDto.builder()
                    .playlistId(job.getTargetPlaylistId())
                    .playlistUrl(job.getTargetPlaylistUrl())
                    .name(job.getSourcePlaylistName() + " (PlaySwap)")
                    .build());
        } else {
            String playlistName = (job.getSourcePlaylistName() != null ? job.getSourcePlaylistName() : "Playlist") + " (PlaySwap)";
            String description = "Transferred via PlaySwap from " + session.getSourcePlatform() + " to " + session.getTargetPlatform();

            if (targetPlatform.contains("SPOTIFY")) {
                playlistMono = spotifyOperations.createPlaylist(userIdStr, playlistName, description);
            } else {
                playlistMono = ytmusicOperations.createPlaylist(userIdStr, playlistName, description);
            }
        }

        return playlistMono
                .flatMap(created -> {
                    // Persist target playlist details for idempotency
                    job.setTargetPlaylistId(created.getPlaylistId());
                    job.setTargetPlaylistUrl(created.getPlaylistUrl());
                    jobRepository.save(job);

                    List<String> trackIdsToBatch = eligibleItems.stream()
                            .filter(item -> !ItemStatus.SUCCESS.name().equalsIgnoreCase(item.getStatus()))
                            .map(TransferItem::getTargetTrackId)
                            .distinct()
                            .collect(Collectors.toList());

                    Mono<Set<String>> addTracksMono;
                    if (trackIdsToBatch.isEmpty()) {
                        addTracksMono = Mono.just(Set.of());
                    } else if (targetPlatform.contains("SPOTIFY")) {
                        addTracksMono = spotifyOperations.addTracksToPlaylistWithResults(
                                userIdStr, created.getPlaylistId(), trackIdsToBatch);
                    } else {
                        addTracksMono = ytmusicOperations.addTracksToPlaylistWithResults(
                                userIdStr, created.getPlaylistId(), trackIdsToBatch);
                    }

                    return addTracksMono
                            .onErrorResume(error -> {
                                log.error("Track insertion failed for job {}: {}", job.getId(), error.getMessage());
                                return Mono.just(Set.of());
                            })
                            .flatMap(insertedNow -> {
                                Set<String> insertedTrackIds = getSuccessfulTrackIds(allItems);
                                insertedTrackIds.addAll(insertedNow);
                                updateItemInsertionResults(allItems, trackIdsToBatch, insertedTrackIds);

                                if (!meetsInsertionThreshold(insertedTrackIds.size(), matchedTrackIds.size())) {
                                    return rollbackFailedTransfer(userIdStr, job, targetPlatform, allItems,
                                            created.getPlaylistId(), created.getPlaylistUrl(),
                                            insertedTrackIds.size(), matchedTrackIds.size());
                                }

                                job.setStatus(insertedTrackIds.size() == matchedTrackIds.size()
                                        ? TransferStatus.COMPLETED.name()
                                        : TransferStatus.PARTIALLY_COMPLETED.name());
                                jobRepository.save(job);
                                saveHistoryRecord(userIdStr, session, job, created,
                                        insertedTrackIds.size(), allItems.size());
                                return Mono.just(TransferredPlaylistDto.builder()
                                        .jobId(job.getId())
                                        .playlistName(created.getName() != null ? created.getName() : job.getSourcePlaylistName())
                                        .targetPlaylistId(created.getPlaylistId())
                                        .targetPlaylistUrl(created.getPlaylistUrl())
                                        .transferredTracksCount(insertedTrackIds.size())
                                        .build());
                            });
                })
                .onErrorResume(e -> {
                    log.error("Failed executing job {}: {}", job.getId(), e.getMessage());
                    job.setStatus(TransferStatus.FAILED.name());
                    jobRepository.save(job);
                    return Mono.empty();
                });
    }

    private Set<String> getSuccessfulTrackIds(List<TransferItem> items) {
        return items.stream()
                .filter(item -> ItemStatus.SUCCESS.name().equalsIgnoreCase(item.getStatus()))
                .map(TransferItem::getTargetTrackId)
                .filter(id -> id != null && !id.isBlank())
                .collect(Collectors.toSet());
    }

    private void updateItemInsertionResults(
            List<TransferItem> allItems,
            List<String> attemptedTrackIds,
            Set<String> insertedTrackIds) {
        Set<String> attempted = new HashSet<>(attemptedTrackIds);
        for (TransferItem item : allItems) {
            String targetTrackId = item.getTargetTrackId();
            if (targetTrackId == null || targetTrackId.isBlank()) {
                continue;
            }

            if (insertedTrackIds.contains(targetTrackId)) {
                item.setStatus(ItemStatus.SUCCESS.name());
                item.setErrorMessage(null);
                itemRepository.save(item);
            } else if (attempted.contains(targetTrackId)) {
                item.setStatus(ItemStatus.FAILED.name());
                item.setRetryCount(item.getRetryCount() != null ? item.getRetryCount() + 1 : 1);
                item.setErrorMessage("Destination platform did not confirm track insertion");
                itemRepository.save(item);
            }
        }
    }

    private boolean meetsInsertionThreshold(int insertedTrackCount, int matchedTrackCount) {
        return matchedTrackCount > 0
                && insertedTrackCount * 100L >= matchedTrackCount * MINIMUM_INSERTION_PERCENT;
    }

    private Mono<TransferredPlaylistDto> rollbackFailedTransfer(
            String userIdStr,
            TransferJob job,
            String targetPlatform,
            List<TransferItem> allItems,
            String targetPlaylistId,
            String targetPlaylistUrl,
            int insertedTrackCount,
            int matchedTrackCount) {
        Mono<Boolean> deletePlaylist;
        if (targetPlaylistId == null || targetPlaylistId.isBlank()) {
            deletePlaylist = Mono.just(true);
        } else if (targetPlatform.contains("SPOTIFY")) {
            deletePlaylist = spotifyOperations.deletePlaylist(userIdStr, targetPlaylistId);
        } else {
            deletePlaylist = ytmusicOperations.deletePlaylist(userIdStr, targetPlaylistId);
        }

        return deletePlaylist
                .defaultIfEmpty(false)
                .onErrorResume(error -> {
                    log.error("Could not roll back destination playlist {} for job {}: {}",
                            targetPlaylistId, job.getId(), error.getMessage());
                    return Mono.just(false);
                })
                .map(deleted -> {
                    String failureReason = String.format(
                            "Only %d of %d matched tracks were inserted; minimum required is %d%%. Playlist rollback %s.",
                            insertedTrackCount,
                            matchedTrackCount,
                            MINIMUM_INSERTION_PERCENT,
                            deleted ? "succeeded" : "failed");
                    for (TransferItem item : allItems) {
                        if (item.getTargetTrackId() == null || item.getTargetTrackId().isBlank()) {
                            continue;
                        }
                        boolean itemWasInserted = ItemStatus.SUCCESS.name().equalsIgnoreCase(item.getStatus());
                        if (deleted) {
                            if (itemWasInserted) {
                                item.setRetryCount(item.getRetryCount() != null ? item.getRetryCount() + 1 : 1);
                            }
                            item.setStatus(ItemStatus.FAILED.name());
                            item.setErrorMessage(failureReason);
                        } else if (!itemWasInserted) {
                            item.setStatus(ItemStatus.FAILED.name());
                            item.setErrorMessage(failureReason);
                        }
                        itemRepository.save(item);
                    }

                    if (deleted) {
                        job.setTargetPlaylistId(null);
                        job.setTargetPlaylistUrl(null);
                    } else {
                        job.setTargetPlaylistId(targetPlaylistId);
                        job.setTargetPlaylistUrl(targetPlaylistUrl);
                    }
                    job.setStatus(TransferStatus.FAILED.name());
                    job.setRetryCount((job.getRetryCount() != null ? job.getRetryCount() : 0) + 1);
                    jobRepository.save(job);

                    return TransferredPlaylistDto.builder()
                            .jobId(job.getId())
                            .playlistName(job.getSourcePlaylistName() != null ? job.getSourcePlaylistName() : "Playlist")
                            .targetPlaylistId(deleted ? null : targetPlaylistId)
                            .targetPlaylistUrl(deleted ? null : targetPlaylistUrl)
                            .transferredTracksCount(0)
                            .build();
                });
    }

    private void saveHistoryRecord(String userIdStr, TransferSession session, TransferJob job, CreatePlaylistResponseDto created, int transferredCount, int totalCount) {
        try {
            UUID userUuid = session.getUserId();
            if (userUuid == null && userIdStr != null) {
                userUuid = UUID.fromString(userIdStr);
            }
            if (userUuid != null) {
                TransferHistory historyRecord = TransferHistory.builder()
                        .userId(userUuid)
                        .sessionId(session.getId())
                        .jobId(job.getId())
                        .sourcePlatform(session.getSourcePlatform())
                        .targetPlatform(session.getTargetPlatform())
                        .sourcePlaylistId(job.getSourcePlaylistId())
                        .sourcePlaylistName(job.getSourcePlaylistName())
                        .targetPlaylistId(created.getPlaylistId())
                        .targetPlaylistName(created.getName())
                        .targetPlaylistUrl(created.getPlaylistUrl())
                        .totalTracks(totalCount)
                        .transferredTracksCount(transferredCount)
                        .status(job.getStatus())
                        .createdAt(LocalDateTime.now())
                        .build();
                transferHistoryRepository.save(historyRecord);
            }
        } catch (Exception e) {
            log.error("Failed to save record to transfer_history table: {}", e.getMessage());
        }
    }
}
