package com.justice.playswap.ytmusic.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePlaylistResponse {
    private String playlistId;
    private String name;
    private String playlistUrl;
}
