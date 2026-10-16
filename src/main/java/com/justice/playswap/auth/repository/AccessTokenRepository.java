package com.justice.playswap.auth.repository;

import com.justice.playswap.auth.entity.AccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccessTokenRepository extends JpaRepository<AccessToken, UUID> {

    Optional<AccessToken> findByUserIdAndPlatform(UUID userId, String platform);

    List<AccessToken> findAllByUserId(UUID userId);

    void deleteByUserIdAndPlatform(UUID userId, String platform);
}
