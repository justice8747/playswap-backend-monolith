package com.justice.playswap.ytmusic.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackAnalysisRequest {
    private String spotifyTrackId;
    private String title;
    private String artist;
    private String primaryArtist;
}
