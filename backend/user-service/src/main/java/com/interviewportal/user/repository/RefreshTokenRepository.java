package com.interviewportal.user.repository;

import com.interviewportal.user.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * Data access for {@link RefreshToken}, including bulk revocation used on logout / ban.
 */
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Query("update RefreshToken r set r.revoked = true where r.userId = :userId")
    void revokeAllForUser(@Param("userId") Long userId);
}
