package com.justice.playswap.transfer.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transfer_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "session_id")
    private UUID sessionId;

    @Column(name = "job_id")
    private UUID jobId;

    @Column(name = "source_platform")
    private String sourcePlatform;

    @Column(name = "target_platform")
    private String targetPlatform;

    @Column(name = "source_playlist_id")
    private String sourcePlaylistId;

    @Column(name = "source_playlist_name")
    private String sourcePlaylistName;

    @Column(name = "target_playlist_id")
    private String targetPlaylistId;

    @Column(name = "target_playlist_name")
    private String targetPlaylistName;

    @Column(name = "target_playlist_url")
    private String targetPlaylistUrl;

    @Column(name = "total_tracks")
    private Integer totalTracks;

    @Column(name = "transferred_tracks_count")
    private Integer transferredTracksCount;

    private String status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
