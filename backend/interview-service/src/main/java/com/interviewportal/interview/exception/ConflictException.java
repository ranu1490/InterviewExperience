package com.interviewportal.interview.exception;

import org.springframework.http.HttpStatus;

/** Thrown on uniqueness violations (e.g. liking twice). Maps to HTTP 409. */
public class ConflictException extends ApiException {
    public ConflictException(String message) {
        super(HttpStatus.CONFLICT, message);
    }
}
