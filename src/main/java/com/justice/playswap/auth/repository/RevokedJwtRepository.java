package com.justice.playswap.auth.repository;

import com.justice.playswap.auth.entity.RevokedJwt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface RevokedJwtRepository extends JpaRepository<RevokedJwt, String> {

    boolean existsByTokenHashAndExpiresAtAfter(String tokenHash, Instant now);

    void deleteAllByExpiresAtBefore(Instant now);
}
