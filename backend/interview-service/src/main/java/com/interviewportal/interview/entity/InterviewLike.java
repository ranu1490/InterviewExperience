package com.interviewportal.interview.entity;

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
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

/**
 * A single "like" of an interview by a user.
 *
 * <p>The composite {@code UNIQUE(interview_id, user_id)} constraint is what prevents duplicate
 * likes — enforced by the database, so it holds even under concurrent requests (a race that an
 * application-level check alone could miss).
 */
@Entity
@Table(name = "interview_likes",
        uniqueConstraints = @UniqueConstraint(name = "uk_like_interview_user",
                columnNames = {"interview_id", "user_id"}),
        indexes = @Index(name = "idx_like_interview", columnList = "interview_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InterviewLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "interview_id", nullable = false)
    private Long interviewId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}
