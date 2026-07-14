package com.interviewportal.user.security;

import com.interviewportal.user.exception.UnauthorizedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Small helper to read the authenticated user's id from the security context. Encapsulating this
 * avoids repeating the (slightly awkward) {@code SecurityContextHolder} plumbing in every service.
 */
public final class CurrentUser {

    private CurrentUser() {
    }

    public static Long requireId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
            throw new UnauthorizedException("Authentication required");
        }
        return userId;
    }
}
