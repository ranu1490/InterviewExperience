package com.interviewportal.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

/**
 * A persisted refresh token, enabling server-side revocation and rotation.
 *
 * <p>Access tokens are stateless and cannot be revoked before they expire — that is fine because
 * they are short lived. Refresh tokens are long lived, so we DO track them: on each refresh we
 * revoke the old token and issue a new one (rotation), and on logout we revoke it. This limits the
 * blast radius of a stolen refresh token.
 *
 * <p>The raw token is indexed+unique for O(1) lookup on refresh.
 */
@Entity
@Table(name = "refresh_tokens",
        uniqueConstraints = @UniqueConstraint(name = "uk_refresh_token", columnNames = "token"),
        indexes = {
                @Index(name = "idx_refresh_token", columnList = "token"),
                @Index(name = "idx_refresh_user", columnList = "user_id")
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 1000)
    private String token;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Instant expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean revoked = false;
}
