package com.interviewportal.interview.exception;

import org.springframework.http.HttpStatus;

/**
 * Base for deliberately-thrown application errors, carrying the HTTP status to return. Keeps the
 * mapping from domain failures to HTTP responses in one place.
 */
public class ApiException extends RuntimeException {

    private final HttpStatus status;

    public ApiException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
