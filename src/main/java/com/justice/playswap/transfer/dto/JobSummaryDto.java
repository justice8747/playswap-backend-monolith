package com.justice.playswap.transfer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JobSummaryDto {
    private UUID jobId;
    private String playlistId;
    private String playlistName;
    private int totalTracks;
    private int matchedCount;
    private int unmatchedCount;
}
