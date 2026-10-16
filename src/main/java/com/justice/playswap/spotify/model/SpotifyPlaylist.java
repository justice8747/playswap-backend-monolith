package com.justice.playswap.spotify.model;

import lombok.Data;
import java.util.List;

@Data
public class SpotifyPlaylist {
    private String id;
    private String name;
    private String description;
    private int totalTracks;
    private String imageUrl;
}

