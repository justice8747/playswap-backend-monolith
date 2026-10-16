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
                    boolean anyFailed = updatedJobs.stream().anyMatch(j -> TransferStatus.FAILED.name().equalsIgnoreCase(j.getStatus()) || TransferStatus.PARTIALLY_COMPLETED.name().equalsIgnoreCase(j.getStatus()));
                    boolean allCompleted = updatedJobs.stream().allMatch(j -> TransferStatus.COMPLETED.name().equalsIgnoreCase(j.getStatus()));

                    String finalStatus = allCompleted ? TransferStatus.COMPLETED.name() : (anyFailed ? TransferStatus.PARTIALLY_COMPLETED.name() : TransferStatus.COMPLETED.name());
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

        if (eligibleItems.isEmpty()) {
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

                    // Transfer items with granular error handling
                    List<String> trackIdsToBatch = eligibleItems.stream()
                            .filter(item -> !ItemStatus.SUCCESS.name().equalsIgnoreCase(item.getStatus()))
                            .map(TransferItem::getTargetTrackId)
                            .distinct()
                            .collect(Collectors.toList());

                    if (trackIdsToBatch.isEmpty()) {
                        // All eligible items were already transferred
                        job.setStatus(TransferStatus.COMPLETED.name());
                        jobRepository.save(job);
                        long successCount = allItems.stream().filter(i -> ItemStatus.SUCCESS.name().equalsIgnoreCase(i.getStatus())).count();
                        return Mono.just(TransferredPlaylistDto.builder()
                                .jobId(job.getId())
                                .playlistName(created.getName() != null ? created.getName() : job.getSourcePlaylistName())
                                .targetPlaylistId(created.getPlaylistId())
                                .targetPlaylistUrl(created.getPlaylistUrl())
                                .transferredTracksCount((int) successCount)
                                .build());
                    }

                    Mono<Boolean> addTracksMono;
                    if (targetPlatform.contains("SPOTIFY")) {
                        addTracksMono = spotifyOperations.addTracksToPlaylist(userIdStr, created.getPlaylistId(), trackIdsToBatch);
                    } else {
                        addTracksMono = ytmusicOperations.addTracksToPlaylist(userIdStr, created.getPlaylistId(), trackIdsToBatch);
                    }

                    return addTracksMono
                            .map(success -> {
                                for (TransferItem item : eligibleItems) {
                                    if (!ItemStatus.SUCCESS.name().equalsIgnoreCase(item.getStatus())) {
                                        item.setStatus(ItemStatus.SUCCESS.name());
                                        item.setErrorMessage(null);
                                        itemRepository.save(item);
                                    }
                                }
                                job.setStatus(TransferStatus.COMPLETED.name());
                                jobRepository.save(job);
                                saveHistoryRecord(userIdStr, session, job, created, eligibleItems.size(), allItems.size());
                                return TransferredPlaylistDto.builder()
                                        .jobId(job.getId())
                                        .playlistName(created.getName() != null ? created.getName() : job.getSourcePlaylistName())
                                        .targetPlaylistId(created.getPlaylistId())
                                        .targetPlaylistUrl(created.getPlaylistUrl())
                                        .transferredTracksCount(eligibleItems.size())
                                        .build();
                            })
                            .onErrorResume(e -> {
                                log.error("Batch add tracks failed for job {}: {}. Updating item-level retry state.", job.getId(), e.getMessage());
                                for (TransferItem item : eligibleItems) {
                                    if (!ItemStatus.SUCCESS.name().equalsIgnoreCase(item.getStatus())) {
                                        int retries = item.getRetryCount() != null ? item.getRetryCount() + 1 : 1;
                                        item.setRetryCount(retries);
                                        item.setErrorMessage(e.getMessage());
                                        item.setStatus(ItemStatus.FAILED.name());
                                        itemRepository.save(item);
                                    }
                                }
                                job.setRetryCount((job.getRetryCount() != null ? job.getRetryCount() : 0) + 1);
                                job.setStatus(TransferStatus.PARTIALLY_COMPLETED.name());
                                jobRepository.save(job);

                                long successCount = allItems.stream().filter(i -> ItemStatus.SUCCESS.name().equalsIgnoreCase(i.getStatus())).count();
                                return Mono.just(TransferredPlaylistDto.builder()
                                        .jobId(job.getId())
                                        .playlistName(created.getName() != null ? created.getName() : job.getSourcePlaylistName())
                                        .targetPlaylistId(created.getPlaylistId())
                                        .targetPlaylistUrl(created.getPlaylistUrl())
                                        .transferredTracksCount((int) successCount)
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
