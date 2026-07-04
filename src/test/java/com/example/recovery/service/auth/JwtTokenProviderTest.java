package com.example.recovery.service.auth;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTokenProviderTest {

    @Test
    void accessTokenShouldContainUserAndSessionClaims() {
        JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(
                "test-secret-key-that-is-long-enough-for-hmac-256",
                300,
                1200
        );

        String token = jwtTokenProvider.createAccessToken(1L, "session-1");
        Claims claims = jwtTokenProvider.parse(token);

        assertTrue(jwtTokenProvider.isAccessToken(claims));
        assertEquals(1L, jwtTokenProvider.extractUserId(claims));
        assertEquals("session-1", jwtTokenProvider.extractSessionId(claims));
    }
}

