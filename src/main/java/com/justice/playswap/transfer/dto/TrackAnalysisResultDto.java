package com.justice.playswap.transfer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackAnalysisResultDto {
    private String spotifyTrackId;
    private String title;
    private String artist;
    private boolean matched;
    private String youtubeVideoId;
    private int confidence;
    private String failureReason;
}
