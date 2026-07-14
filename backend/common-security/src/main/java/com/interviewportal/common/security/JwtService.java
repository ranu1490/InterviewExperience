package com.interviewportal.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Framework-agnostic helper that creates and validates JSON Web Tokens.
 *
 * <p>Why a shared service: every microservice must verify the exact same tokens, so the
 * signing/parsing logic lives in one library ({@code common-security}) and is reused by the
 * gateway, user-service and interview-service. This guarantees they never drift apart.
 *
 * <p>Design choices:
 * <ul>
 *   <li><b>HMAC-SHA256 (HS256)</b> with a shared secret — simplest option for a symmetric,
 *       single-organisation setup. Alternative: RS256 with a public/private key pair, which is
 *       preferable when third parties must verify tokens without holding the signing key.</li>
 *   <li><b>Stateless</b> — all identity data (id, email, roles) is embedded as claims so no
 *       service call or session store is needed to authenticate a request. This is the key
 *       enabler for horizontal scaling to ~1M users.</li>
 * </ul>
 */
public class JwtService {

    public static final String CLAIM_ROLES = "roles";
    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_USERNAME = "username";
    public static final String CLAIM_TOKEN_TYPE = "type";
    public static final String TOKEN_TYPE_ACCESS = "access";
    public static final String TOKEN_TYPE_REFRESH = "refresh";

    private final JwtProperties properties;
    private final SecretKey signingKey;

    public JwtService(JwtProperties properties) {
        this.properties = properties;
        this.signingKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    /** Builds a short-lived access token that carries the user's identity and roles. */
    public String generateAccessToken(Long userId, String username, String email, List<String> roles) {
        return buildToken(userId, username, email, roles, TOKEN_TYPE_ACCESS,
                properties.getAccessTokenExpirationMs());
    }

    /** Builds a long-lived refresh token used only to mint new access tokens. */
    public String generateRefreshToken(Long userId, String username, String email, List<String> roles) {
        return buildToken(userId, username, email, roles, TOKEN_TYPE_REFRESH,
                properties.getRefreshTokenExpirationMs());
    }

    private String buildToken(Long userId, String username, String email, List<String> roles,
                              String type, long expirationMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .id(java.util.UUID.randomUUID().toString())   // unique jti: guarantees no two tokens collide
                .subject(String.valueOf(userId))
                .issuer(properties.getIssuer())
                .issuedAt(now)
                .expiration(expiry)
                .claims(Map.of(
                        CLAIM_USERNAME, username == null ? "" : username,
                        CLAIM_EMAIL, email == null ? "" : email,
                        CLAIM_ROLES, roles,
                        CLAIM_TOKEN_TYPE, type))
                .signWith(signingKey)
                .compact();
    }

    /**
     * Parses and cryptographically verifies a token.
     *
     * @throws io.jsonwebtoken.JwtException if the signature is invalid or the token is expired
     */
    public Jws<Claims> parse(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .requireIssuer(properties.getIssuer())
                .build()
                .parseSignedClaims(token);
    }

    /** Returns true when the token is well-formed, correctly signed and not expired. */
    public boolean isValid(String token) {
        try {
            parse(token);
            return true;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    public Long extractUserId(String token) {
        return Long.valueOf(parse(token).getPayload().getSubject());
    }

    @SuppressWarnings("unchecked")
    public List<String> extractRoles(String token) {
        Object roles = parse(token).getPayload().get(CLAIM_ROLES);
        return roles instanceof List ? (List<String>) roles : List.of();
    }

    public String extractUsername(String token) {
        return parse(token).getPayload().get(CLAIM_USERNAME, String.class);
    }

    public String extractEmail(String token) {
        return parse(token).getPayload().get(CLAIM_EMAIL, String.class);
    }

    public String extractTokenType(String token) {
        return parse(token).getPayload().get(CLAIM_TOKEN_TYPE, String.class);
    }
}
