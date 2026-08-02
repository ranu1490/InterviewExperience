package com.interviewportal.interview.repository;

import com.interviewportal.interview.entity.InterviewLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/** Data access for likes, including the lookups used to prevent duplicates and hydrate "liked by me". */
public interface InterviewLikeRepository extends JpaRepository<InterviewLike, Long> {

    Optional<InterviewLike> findByInterviewIdAndUserId(Long interviewId, Long userId);

    boolean existsByInterviewIdAndUserId(Long interviewId, Long userId);

    /** Which of these interviews has the given user liked — one query to decorate a whole page. */
    List<InterviewLike> findByUserIdAndInterviewIdIn(Long userId, Collection<Long> interviewIds);

    void deleteByInterviewId(Long interviewId);
}
