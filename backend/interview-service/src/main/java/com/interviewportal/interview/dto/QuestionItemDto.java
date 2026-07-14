package com.interviewportal.interview.dto;

import com.interviewportal.interview.entity.QuestionCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/** One question plus its topic, as sent/received over the API. */
public record QuestionItemDto(
        @NotNull QuestionCategory category,
        @NotBlank String question
) {
}
