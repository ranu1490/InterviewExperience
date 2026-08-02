package com.interviewportal.interview.dto;

import java.time.Instant;

/** A comment as returned to clients. */
public record CommentResponse(
        Long id,
        Long interviewId,
        Long userId,
        String username,
        String content,
        Instant createdAt
) {
}
