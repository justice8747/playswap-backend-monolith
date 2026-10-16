package com.justice.playswap.transfer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferSessionHistoryDto {
    private UUID id;
    private UUID sessionId;
    private String sourcePlatform;
    private String targetPlatform;
    private String status;
    private LocalDateTime createdAt;
    private int totalPlaylists;
    private int totalTracks;
    private int matchedTracks;
    private int unmatchedTracks;
    private List<JobSummaryDto> jobs;
}
