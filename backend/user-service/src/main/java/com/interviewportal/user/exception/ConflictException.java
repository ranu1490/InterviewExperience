package com.interviewportal.user.exception;

import org.springframework.http.HttpStatus;

/** Thrown when a request violates a uniqueness rule (e.g. email already registered). Maps to HTTP 409. */
public class ConflictException extends ApiException {
    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
