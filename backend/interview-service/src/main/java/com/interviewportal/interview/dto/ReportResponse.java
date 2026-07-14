package com.interviewportal.interview.dto;

import com.interviewportal.interview.entity.ReportStatus;

import java.time.Instant;

/** A report as shown to admins in the moderation queue. */
public record ReportResponse(
        Long id,
        Long interviewId,
        Long reporterUserId,
        String reason,
        ReportStatus status,
        Instant createdAt
) {
}
