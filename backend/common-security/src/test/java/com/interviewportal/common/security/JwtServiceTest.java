package com.interviewportal.common.security;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private final JwtService jwtService = new JwtService(new JwtProperties());

    @Test
    void generatesAndValidatesAccessToken() {
        String token = jwtService.generateAccessToken(42L, "alice", "alice@example.com",
                List.of("USER"));

        assertTrue(jwtService.isValid(token));
        assertEquals(42L, jwtService.extractUserId(token));
        assertEquals("alice", jwtService.extractUsername(token));
        assertEquals("alice@example.com", jwtService.extractEmail(token));
        assertEquals(List.of("USER"), jwtService.extractRoles(token));
        assertEquals(JwtService.TOKEN_TYPE_ACCESS, jwtService.extractTokenType(token));
    }

    @Test
    void rejectsTamperedToken() {
        String token = jwtService.generateAccessToken(1L, "bob", "bob@example.com", List.of("USER"));
        assertFalse(jwtService.isValid(token + "tampered"));
    }

    @Test
    void refreshTokenHasRefreshType() {
        String token = jwtService.generateRefreshToken(1L, "bob", "bob@example.com", List.of("USER"));
        assertEquals(JwtService.TOKEN_TYPE_REFRESH, jwtService.extractTokenType(token));
    }
}
