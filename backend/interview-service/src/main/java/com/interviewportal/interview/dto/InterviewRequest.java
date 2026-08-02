package com.interviewportal.interview.dto;

import com.interviewportal.interview.entity.ExperienceLevel;
import com.interviewportal.interview.entity.SelectionStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

/**
 * Payload for creating or updating an interview experience. Reused for both operations because the
 * writable fields are identical — a single validated DTO is simpler than two near-duplicates.
 *
 * <p>Note the AI fields (difficulty, summary, topics) are intentionally NOT here: they are derived
 * server-side, never supplied by the client, so they cannot be spoofed.
 */
public record InterviewRequest(
        @NotBlank @Size(max = 150) String companyName,
        @Size(max = 500) String companyLogo,
        @NotBlank @Size(max = 150) String jobRole,
        @NotNull ExperienceLevel experienceLevel,
        @PositiveOrZero Double yearsOfExperience,
        LocalDate interviewDate,
        @Size(max = 150) String location,
        @Size(max = 60) String ctcOffered,
        Integer numberOfRounds,
        @Valid List<RoundDetailDto> rounds,
        @Valid List<QuestionItemDto> questions,
        String overallExperience,
        String preparationTips,
        List<String> resourcesUsed,
        @NotNull SelectionStatus selectionStatus,
        Set<String> tags
) {
}
