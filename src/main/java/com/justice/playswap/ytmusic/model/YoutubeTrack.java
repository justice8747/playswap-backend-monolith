package com.justice.playswap.ytmusic.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class YoutubeTrack {
    private String trackId;
    private String title;
    private String artists;
    private String primaryArtist;
    private String album;
    private long durationMs;
    private String isrc;
}
