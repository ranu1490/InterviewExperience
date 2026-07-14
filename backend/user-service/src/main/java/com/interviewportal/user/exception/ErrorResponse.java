package com.interviewportal.user.exception;

import java.time.Instant;
import java.util.Map;

/**
 * Uniform error body returned for every failure, so clients can parse errors consistently.
 * {@code fieldErrors} is populated only for validation failures.
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
}
