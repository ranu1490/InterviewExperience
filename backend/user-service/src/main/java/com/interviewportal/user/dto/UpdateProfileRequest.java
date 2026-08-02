package com.interviewportal.user.dto;

import jakarta.validation.constraints.Size;

/** Fields a user may change on their own profile. Identity fields (email/username) are immutable here. */
public record UpdateProfileRequest(
        @Size(max = 100) String fullName,
        @Size(max = 500) String bio,
        @Size(max = 500) String avatarUrl
) {
}
