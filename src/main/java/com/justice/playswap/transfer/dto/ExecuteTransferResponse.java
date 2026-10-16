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
public class ExecuteTransferResponse {
    private UUID sessionId;
    private String status;
    private int totalPlaylists;
    private int totalTransferredTracks;
    private List<TransferredPlaylistDto> transferredPlaylists;
}
