package com.interviewportal.interview.dto;

import jakarta.validation.constraints.NotBlank;

/** One interview round, as sent/received over the API. */
public record RoundDetailDto(
        int roundNumber,
        @NotBlank String name,
        String description
) {
}
