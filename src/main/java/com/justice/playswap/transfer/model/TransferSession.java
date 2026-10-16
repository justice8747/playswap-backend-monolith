package com.justice.playswap.transfer.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transfer_sessions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransferSession {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(name = "source_platform")
    private String sourcePlatform;

    @Column(name = "target_platform")
    private String targetPlatform;

    @Column(name = "idempotency_key")
    private String idempotencyKey;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
}
