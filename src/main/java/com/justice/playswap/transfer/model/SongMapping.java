package com.justice.playswap.transfer.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "song_mappings",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"source_platform", "source_track_id", "destination_platform"})
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SongMapping {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "source_platform", nullable = false)
    private String sourcePlatform;

    @Column(name = "source_track_id", nullable = false)
    private String sourceTrackId;

    @Column(name = "destination_platform", nullable = false)
    private String destinationPlatform;

    @Column(name = "destination_track_id", nullable = false)
    private String destinationTrackId;

    private String title;
    private String artist;

    @Column(name = "confidence_score")
    private Integer confidenceScore;

    @Column(name = "last_used_at")
    private LocalDateTime lastUsedAt;
}
