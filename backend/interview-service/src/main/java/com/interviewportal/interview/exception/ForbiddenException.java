package com.interviewportal.interview.exception;

import org.springframework.http.HttpStatus;

/** Thrown when a user tries to modify a resource they do not own (and is not admin). Maps to HTTP 403. */
public class ForbiddenException extends ApiException {
    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
