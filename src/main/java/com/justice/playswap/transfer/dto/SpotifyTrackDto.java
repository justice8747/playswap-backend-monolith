package com.justice.playswap.transfer.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpotifyTrackDto {
    private String trackId;
    private String title;
    private String artists;
    private String primaryArtist;
    private String album;
    private long durationMs;
    private String isrc;
}
