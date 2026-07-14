package com.interviewportal.interview.security;

/**
 * The authenticated caller, reconstructed from the JWT. Carries the username so the service can
 * denormalise author/comment names without calling the user-service on every write.
 */
public record AuthPrincipal(Long id, String username, String email) {
}
