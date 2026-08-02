package com.interviewportal.interview.exception;

import org.springframework.http.HttpStatus;

/** Thrown when authentication is required but absent/invalid. Maps to HTTP 401. */
public class UnauthorizedException extends ApiException {
    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
