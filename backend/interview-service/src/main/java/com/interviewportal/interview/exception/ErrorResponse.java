package com.interviewportal.interview.exception;

import java.time.Instant;
import java.util.Map;

/** Uniform error body, identical in shape to the user-service so clients parse errors the same way. */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        String path,
        Map<String, String> fieldErrors
) {
}
