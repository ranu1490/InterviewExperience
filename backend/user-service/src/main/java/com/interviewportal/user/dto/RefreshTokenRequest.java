package com.interviewportal.user.dto;

import jakarta.validation.constraints.NotBlank;

/** Payload for exchanging a valid refresh token for a fresh access/refresh pair. */
public record RefreshTokenRequest(@NotBlank String refreshToken) {
}
