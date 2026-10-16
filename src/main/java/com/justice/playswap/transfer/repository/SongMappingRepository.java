package com.justice.playswap.transfer.repository;

import com.justice.playswap.transfer.model.SongMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SongMappingRepository extends JpaRepository<SongMapping, UUID> {
    Optional<SongMapping> findBySourcePlatformAndSourceTrackIdAndDestinationPlatform(
            String sourcePlatform, String sourceTrackId, String destinationPlatform);
}
