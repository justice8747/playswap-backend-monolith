package com.justice.playswap.transfer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyzePlaylistsRequest {
    private String sourcePlatform;
    private String targetPlatform;
    private List<String> playlistIds;
    private Map<String, String> playlistNames;
}
