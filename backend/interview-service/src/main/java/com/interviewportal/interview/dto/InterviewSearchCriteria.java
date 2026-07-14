package com.interviewportal.interview.dto;

import com.interviewportal.interview.entity.DifficultyLabel;
import com.interviewportal.interview.entity.ExperienceLevel;
import com.interviewportal.interview.entity.SelectionStatus;

import java.time.LocalDate;

/**
 * All optional filters a search can combine. Any {@code null} field is simply ignored, so callers
 * mix and match freely ("multiple filters simultaneously"). Grouping the filters in one immutable
 * object keeps the service signature clean and makes the criteria easy to pass around and test.
 */
public record InterviewSearchCriteria(
        String keyword,
        String company,
        String role,
        ExperienceLevel experienceLevel,
        Double minYearsOfExperience,
        Double maxYearsOfExperience,
        DifficultyLabel difficultyLabel,
        SelectionStatus selectionStatus,
        String location,
        String tag,
        LocalDate dateFrom,
        LocalDate dateTo
) {
}
