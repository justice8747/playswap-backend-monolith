package com.justice.playswap.transfer.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;

@Entity
@Table(name = "transfer_items")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferItem {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "job_id")
    private UUID jobId;

    @Column(name = "source_track_id", nullable = false)
    private String sourceTrackId;

    @Column(name = "target_track_id")
    private String targetTrackId;

    @Column(name = "status")
    private String status;

    @Column(name = "confidence_score")
    private Integer confidenceScore;

    @Builder.Default
    @Column(name = "retry_count")
    private Integer retryCount = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "matched_track_uri")
    private String matchedTrackUri;
}
