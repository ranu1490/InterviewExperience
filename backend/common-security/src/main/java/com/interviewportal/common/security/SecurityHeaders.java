package com.interviewportal.common.security;

/**
 * Names of the trusted HTTP headers the API gateway injects after it validates a JWT.
 *
 * <p>Downstream services can read these instead of re-parsing the token, but in this project
 * each service ALSO validates the token itself (defence in depth) so it stays secure even if
 * reached directly. Centralising the header names avoids typos across modules.
 */
public final class SecurityHeaders {

    public static final String USER_ID = "X-User-Id";
    public static final String USERNAME = "X-User-Name";
    public static final String USER_EMAIL = "X-User-Email";
    public static final String USER_ROLES = "X-User-Roles";

    private SecurityHeaders() {
    }
}
