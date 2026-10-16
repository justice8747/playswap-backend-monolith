package com.justice.playswap.spotify.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackAnalysisRequest {
    private String sourceTrackId;
    private String title;
    private String artist;
    private String primaryArtist;
}
