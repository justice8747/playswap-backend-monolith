package com.justice.playswap.transfer.repository;

import com.justice.playswap.transfer.model.TransferJob;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransferJobRepository extends JpaRepository<TransferJob, UUID> {
    List<TransferJob> findBySessionId(UUID sessionId);
}
