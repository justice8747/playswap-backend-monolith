package com.justice.playswap.transfer.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transfer_jobs")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferJob {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "session_id")
    private UUID sessionId;

    @Column(name = "source_playlist_id", nullable = false)
    private String sourcePlaylistId;

    @Column(name = "source_playlist_name")
    private String sourcePlaylistName;

    @Column(name = "target_playlist_id")
    private String targetPlaylistId;

    @Column(name = "target_playlist_url")
    private String targetPlaylistUrl;

    @Column(name = "idempotency_key")
    private String idempotencyKey;

    @Column(name = "status")
    private String status;

    @Builder.Default
    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
