package com.justice.playswap.transfer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalysisSummaryResponse {
    private UUID sessionId;
    private int totalPlaylists;
    private int totalTracks;
    private int matchedTracks;
    private int unmatchedTracks;
    private int cachedMatches;
    private String status;
    private List<JobSummaryDto> jobs;
}
