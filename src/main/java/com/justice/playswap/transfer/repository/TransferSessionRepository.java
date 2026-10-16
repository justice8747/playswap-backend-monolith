package com.justice.playswap.transfer.repository;

import com.justice.playswap.transfer.model.TransferSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransferSessionRepository extends JpaRepository<TransferSession, UUID> {
    List<TransferSession> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
