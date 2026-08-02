package com.interviewportal.interview.repository;

import com.interviewportal.interview.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/** Data access for comments. */
public interface CommentRepository extends JpaRepository<Comment, Long> {

    Page<Comment> findByInterviewIdOrderByCreatedAtDesc(Long interviewId, Pageable pageable);

    void deleteByInterviewId(Long interviewId);
}
