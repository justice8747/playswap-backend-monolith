package com.justice.playswap.transfer.repository;

import com.justice.playswap.transfer.model.TransferItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransferItemRepository extends JpaRepository<TransferItem, UUID> {
    List<TransferItem> findByJobId(UUID jobId);
}
