package com.interviewportal.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Strongly-typed configuration for JWT signing and expiry.
 *
 * <p>Bound from the {@code security.jwt.*} namespace in {@code application.yml}. Using a
 * dedicated properties class (instead of scattered {@code @Value} annotations) keeps all
 * security knobs in one place and makes them trivial to validate and test.
 */
@ConfigurationProperties(prefix = "security.jwt")
public class JwtProperties {

    /** Base64/plain secret used to sign tokens with HMAC-SHA256. MUST be identical across services. */
    private String secret = "change-me-in-production-please-use-a-long-random-secret-key-256bit";

    /** Access-token lifetime in milliseconds. Short lived (default 15 minutes). */
    private long accessTokenExpirationMs = 15 * 60 * 1000L;

    /** Refresh-token lifetime in milliseconds. Long lived (default 7 days). */
    private long refreshTokenExpirationMs = 7L * 24 * 60 * 60 * 1000L;

    /** Token issuer claim, useful when multiple systems share an auth domain. */
    private String issuer = "interview-experience-portal";

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public long getAccessTokenExpirationMs() {
        return accessTokenExpirationMs;
    }

    public void setAccessTokenExpirationMs(long accessTokenExpirationMs) {
        this.accessTokenExpirationMs = accessTokenExpirationMs;
    }

    public long getRefreshTokenExpirationMs() {
        return refreshTokenExpirationMs;
    }

    public void setRefreshTokenExpirationMs(long refreshTokenExpirationMs) {
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    public String getIssuer() {
        return issuer;
    }

    public void setIssuer(String issuer) {
        this.issuer = issuer;
    }
}
