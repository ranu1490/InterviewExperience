package com.interviewportal.user.dto;

import java.time.Instant;
import java.util.Set;

/**
 * Public/self view of a user. Deliberately excludes the password hash and provider id — the DTO
 * pattern lets us expose exactly what a client should see and nothing more.
 */
public record UserResponse(
        Long id,
        String username,
        String email,
        String fullName,
        String bio,
        String avatarUrl,
        String provider,
        Set<String> roles,
        boolean banned,
        Instant createdAt
) {
}
