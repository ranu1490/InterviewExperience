package com.interviewportal.user.exception;

import org.springframework.http.HttpStatus;

/** Thrown when an authenticated user lacks permission for an action (e.g. banned). Maps to HTTP 403. */
public class ForbiddenException extends ApiException {
    public ForbiddenException(String message) {
        super(HttpStatus.FORBIDDEN, message);
    }
}
