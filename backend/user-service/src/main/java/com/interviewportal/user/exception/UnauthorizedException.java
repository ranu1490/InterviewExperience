package com.interviewportal.user.exception;

import org.springframework.http.HttpStatus;

/** Thrown when credentials are missing or invalid. Maps to HTTP 401. */
public class UnauthorizedException extends ApiException {
    public UnauthorizedException(String message) {
        super(HttpStatus.UNAUTHORIZED, message);
    }
}
