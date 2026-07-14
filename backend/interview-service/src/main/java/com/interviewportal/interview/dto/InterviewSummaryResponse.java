package com.interviewportal.interview.dto;

import com.interviewportal.interview.entity.DifficultyLabel;
import com.interviewportal.interview.entity.ExperienceLevel;
import com.interviewportal.interview.entity.SelectionStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Set;

/**
 * Lightweight card view used in listings/search results.
 *
 * <p>Why a separate, slimmer DTO from {@link InterviewResponse}: list endpoints return many rows,
 * so we omit the heavy fields (full question list, long text bodies). This shrinks payloads and,
 * combined with pagination, keeps list responses fast and cheap — an API-optimisation best
 * practice for read-heavy systems.
 */
public record InterviewSummaryResponse(
        Long id,
        String companyName,
        String companyLogo,
        String jobRole,
        ExperienceLevel experienceLevel,
        LocalDate interviewDate,
        String location,
        SelectionStatus selectionStatus,
        Integer difficultyScore,
        DifficultyLabel difficultyLabel,
        Set<String> tags,
        String authorUsername,
        int totalLikes,
        int totalComments,
        long views,
        Instant createdAt
) {
}
