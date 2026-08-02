package com.interviewportal.interview.dto;

import com.interviewportal.interview.entity.DifficultyLabel;
import com.interviewportal.interview.entity.ExperienceLevel;
import com.interviewportal.interview.entity.SelectionStatus;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/** Full detail view of an interview experience, including the AI-derived fields. */
public record InterviewResponse(
        Long id,
        String companyName,
        String companyLogo,
        String jobRole,
        ExperienceLevel experienceLevel,
        Double yearsOfExperience,
        LocalDate interviewDate,
        String location,
        String ctcOffered,
        Integer numberOfRounds,
        List<RoundDetailDto> rounds,
        List<QuestionItemDto> questions,
        String overallExperience,
        String preparationTips,
        List<String> resourcesUsed,
        SelectionStatus selectionStatus,
        Integer difficultyScore,
        DifficultyLabel difficultyLabel,
        String aiSummary,
        List<String> aiSuggestedTopics,
        Set<String> tags,
        Long authorId,
        String authorUsername,
        int totalLikes,
        int totalComments,
        long views,
        boolean likedByCurrentUser,
        Instant createdAt,
        Instant updatedAt
) {
}
