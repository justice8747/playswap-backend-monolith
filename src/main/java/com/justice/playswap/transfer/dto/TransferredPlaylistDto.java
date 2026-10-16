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
public class TransferredPlaylistDto {
    private UUID jobId;
    private String playlistName;
    private String targetPlaylistId;
    private String targetPlaylistUrl;
    private int transferredTracksCount;
}
