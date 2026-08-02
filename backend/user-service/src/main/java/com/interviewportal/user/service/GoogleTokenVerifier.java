package com.interviewportal.user.service;

/**
 * Verifies a Google ID token and extracts the essential profile fields.
 *
 * <p>An interface (not a concrete class) so tests can inject a fake and the real Google call is
 * only made in production — this is the "mockable now, real later" seam requested for AI too.
 */
public interface GoogleTokenVerifier {

    GoogleUserInfo verify(String idToken);

    /** Minimal, verified identity extracted from a Google ID token. */
    record GoogleUserInfo(String subject, String email, String name, String picture) {
    }
}
