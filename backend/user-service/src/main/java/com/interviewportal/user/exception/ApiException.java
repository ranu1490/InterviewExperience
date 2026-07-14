package com.interviewportal.user.exception;

import org.springframework.http.HttpStatus;

/**
 * Base for all deliberately-thrown application errors. Carrying the HTTP status on the exception
 * keeps the mapping between domain failures and HTTP responses in one obvious place and avoids a
 * sprawling {@code if/else} in the controller advice.
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
