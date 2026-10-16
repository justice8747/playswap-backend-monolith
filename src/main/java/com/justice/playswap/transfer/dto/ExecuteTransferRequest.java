package com.justice.playswap.transfer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecuteTransferRequest {
    private UUID sessionId;
    /**
     * Optional explicit selection of track IDs per job.
     * Key: Job UUID, Value: List of target track IDs selected by user.
     * If null/empty, all matched items for the job will be transferred.
     */
    private Map<UUID, List<String>> jobTrackSelections;
}
