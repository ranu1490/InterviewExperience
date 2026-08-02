package com.interviewportal.user.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Login payload. {@code usernameOrEmail} lets users authenticate with either identifier — a small
 * UX win that costs nothing because both columns are indexed.
 */
public record LoginRequest(
        @NotBlank String usernameOrEmail,
        @NotBlank String password
) {
}
