package com.interviewportal.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Payload for local sign-up. Using a record keeps the DTO immutable and boilerplate-free.
 * Bean-Validation annotations enforce input rules at the edge so bad data never reaches the domain.
 */
public record SignupRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Email @Size(max = 320) String email,
        @NotBlank @Size(min = 8, max = 72) String password,
        @Size(max = 100) String fullName
) {
}
