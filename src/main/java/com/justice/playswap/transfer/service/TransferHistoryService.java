package com.justice.playswap.transfer.service;

import com.justice.playswap.transfer.dto.JobSummaryDto;
import com.justice.playswap.transfer.dto.TransferSessionHistoryDto;
import com.justice.playswap.transfer.model.TransferItem;
import com.justice.playswap.transfer.model.TransferJob;
import com.justice.playswap.transfer.model.TransferSession;
import com.justice.playswap.transfer.repository.TransferItemRepository;
import com.justice.playswap.transfer.repository.TransferJobRepository;
import com.justice.playswap.transfer.repository.TransferSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferHistoryService {

    private final TransferSessionRepository sessionRepository;
    private final TransferJobRepository jobRepository;
    private final TransferItemRepository itemRepository;
    private final com.justice.playswap.transfer.repository.TransferHistoryRepository transferHistoryRepository;

    public List<TransferSessionHistoryDto> getUserTransferHistory(String userIdStr) {
        log.info("Fetching transfer history for user '{}'", userIdStr);

        UUID userUuid = null;
        try {
            if (userIdStr != null && !userIdStr.isBlank()) {
                userUuid = UUID.fromString(userIdStr);
            }
        } catch (IllegalArgumentException e) {
            log.warn("Could not parse userId to UUID for history query: {}", userIdStr);
            return List.of();
        }

        if (userUuid == null) {
            return List.of();
        }

        // 1. Query dedicated transfer_history table
        List<com.justice.playswap.transfer.model.TransferHistory> historyRecords = transferHistoryRepository.findByUserIdOrderByCreatedAtDesc(userUuid);
        if (historyRecords != null && !historyRecords.isEmpty()) {
            log.info("Found {} records in transfer_history table for user '{}'", historyRecords.size(), userIdStr);

            Map<UUID, List<com.justice.playswap.transfer.model.TransferHistory>> groupedBySession = historyRecords.stream()
                    .collect(Collectors.groupingBy(h -> h.getSessionId() != null ? h.getSessionId() : h.getId(), LinkedHashMap::new, Collectors.toList()));

            List<TransferSessionHistoryDto> historyDtos = new ArrayList<>();
            for (Map.Entry<UUID, List<com.justice.playswap.transfer.model.TransferHistory>> entry : groupedBySession.entrySet()) {
                List<com.justice.playswap.transfer.model.TransferHistory> sessionRecords = entry.getValue();
                com.justice.playswap.transfer.model.TransferHistory first = sessionRecords.get(0);

                int totalTracks = sessionRecords.stream().mapToInt(h -> h.getTotalTracks() != null ? h.getTotalTracks() : 0).sum();
                int transferredTracks = sessionRecords.stream().mapToInt(h -> h.getTransferredTracksCount() != null ? h.getTransferredTracksCount() : 0).sum();

                List<JobSummaryDto> jobSummaries = sessionRecords.stream().map(h -> JobSummaryDto.builder()
                        .jobId(h.getJobId() != null ? h.getJobId() : h.getId())
                        .playlistId(h.getSourcePlaylistId())
                        .playlistName(h.getTargetPlaylistName() != null ? h.getTargetPlaylistName() : h.getSourcePlaylistName())
                        .totalTracks(h.getTotalTracks() != null ? h.getTotalTracks() : 0)
                        .matchedCount(h.getTransferredTracksCount() != null ? h.getTransferredTracksCount() : 0)
                        .unmatchedCount(Math.max(0, (h.getTotalTracks() != null ? h.getTotalTracks() : 0) - (h.getTransferredTracksCount() != null ? h.getTransferredTracksCount() : 0)))
                        .build()).collect(Collectors.toList());

                historyDtos.add(TransferSessionHistoryDto.builder()
                        .id(first.getSessionId() != null ? first.getSessionId() : first.getId())
                        .sessionId(first.getSessionId() != null ? first.getSessionId() : first.getId())
                        .sourcePlatform(first.getSourcePlatform())
                        .targetPlatform(first.getTargetPlatform())
                        .status(first.getStatus())
                        .createdAt(first.getCreatedAt())
                        .totalPlaylists(sessionRecords.size())
                        .totalTracks(totalTracks)
                        .matchedTracks(transferredTracks)
                        .unmatchedTracks(Math.max(0, totalTracks - transferredTracks))
                        .jobs(jobSummaries)
                        .build());
            }
            return historyDtos;
        }

        // 2. Fallback to sessions/jobs/items tables
        List<TransferSession> sessions = sessionRepository.findByUserIdOrderByCreatedAtDesc(userUuid);
        if (sessions == null || sessions.isEmpty()) {
            log.info("No transfer history found for user UUID '{}'", userUuid);
            return List.of();
        }

        return sessions.stream().map(session -> {
            List<TransferJob> jobs = jobRepository.findBySessionId(session.getId());
            List<JobSummaryDto> jobSummaries = new ArrayList<>();
            int sessionTotalTracks = 0;
            int sessionMatchedTracks = 0;
            int sessionUnmatchedTracks = 0;

            if (jobs != null) {
                for (TransferJob job : jobs) {
                    List<TransferItem> items = itemRepository.findByJobId(job.getId());
                    int total = items != null ? items.size() : 0;
                    int matched = 0;
                    int unmatched = 0;

                    if (items != null) {
                        for (TransferItem item : items) {
                            if ("SUCCESS".equalsIgnoreCase(item.getStatus())) {
                                matched++;
                            } else {
                                unmatched++;
                            }
                        }
                    }

                    sessionTotalTracks += total;
                    sessionMatchedTracks += matched;
                    sessionUnmatchedTracks += unmatched;

                    jobSummaries.add(JobSummaryDto.builder()
                            .jobId(job.getId())
                            .playlistId(job.getSourcePlaylistId())
                            .playlistName(job.getSourcePlaylistName())
                            .totalTracks(total)
                            .matchedCount(matched)
                            .unmatchedCount(unmatched)
                            .build());
                }
            }

            return TransferSessionHistoryDto.builder()
                    .id(session.getId())
                    .sessionId(session.getId())
                    .sourcePlatform(session.getSourcePlatform())
                    .targetPlatform(session.getTargetPlatform())
                    .status(session.getStatus())
                    .createdAt(session.getCreatedAt())
                    .totalPlaylists(jobs != null ? jobs.size() : 0)
                    .totalTracks(sessionTotalTracks)
                    .matchedTracks(sessionMatchedTracks)
                    .unmatchedTracks(sessionUnmatchedTracks)
                    .jobs(jobSummaries)
                    .build();
        }).collect(Collectors.toList());
    }
}
