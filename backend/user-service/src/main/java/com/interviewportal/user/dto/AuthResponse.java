package com.interviewportal.user.dto;

/**
 * Returned by every successful authentication (signup/login/refresh/google).
 * Bundles the token pair with a lightweight view of the user so the client can render the UI
 * without an extra round-trip.
 */
public record AuthResponse(
        String accessToken,
        String refreshToken,
        String tokenType,
        UserResponse user
) {
    public static AuthResponse of(String accessToken, String refreshToken, UserResponse user) {
        return new AuthResponse(accessToken, refreshToken, "Bearer", user);
    }
}
