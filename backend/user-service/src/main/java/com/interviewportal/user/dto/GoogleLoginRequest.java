package com.interviewportal.user.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * Payload for "Sign in with Google". The Angular app uses Google Identity Services to obtain an
 * ID token in the browser, then posts it here. The server verifies the token and issues its own
 * JWTs.
 *
 * <p>Why the ID-token flow instead of a server-side redirect: it is the recommended pattern for
 * SPAs, keeps the backend fully stateless (no session, no callback URLs to manage) and fits the
 * gateway + microservice topology cleanly.
 */
public record GoogleLoginRequest(@NotBlank String idToken) {
}
