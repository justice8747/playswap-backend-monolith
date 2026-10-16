package com.justice.playswap.ytmusic.model;

import lombok.Data;

@Data
public class YoutubePlaylist {
    private String id;
    private String name;
    private String description;
    private int totalTracks;
    private String imageUrl;
}
