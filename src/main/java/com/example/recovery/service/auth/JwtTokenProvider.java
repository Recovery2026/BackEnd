package com.example.recovery.service.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtTokenProvider {
    private final SecretKey secretKey;
    @Getter
    private final long accessTokenSeconds;
    @Getter
    private final long refreshTokenSeconds;

    public JwtTokenProvider(
            @Value("${app.jwt.secret:change-me-to-a-real-secret-key-with-at-least-32-bytes}") String secret,
            @Value("${app.jwt.access-token-seconds:900}") long accessTokenSeconds,
            @Value("${app.jwt.refresh-token-seconds:1209600}") long refreshTokenSeconds
    ) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) {
            throw new IllegalArgumentException("JWT secret length must be at least 32 bytes.");
        }

        this.secretKey = Keys.hmacShaKeyFor(bytes);
        this.accessTokenSeconds = accessTokenSeconds;
        this.refreshTokenSeconds = refreshTokenSeconds;
    }

    public String createAccessToken(Long userId, String sessionId) {
        return createToken(userId, sessionId, "access", accessTokenSeconds);
    }

    public String createRefreshToken(Long userId, String sessionId) {
        return createToken(userId, sessionId, "refresh", refreshTokenSeconds);
    }

    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long extractUserId(Claims claims) {
        return Long.parseLong(claims.getSubject());
    }

    public String extractSessionId(Claims claims) {
        return claims.get("sid", String.class);
    }

    public boolean isAccessToken(Claims claims) {
        return "access".equals(claims.get("typ", String.class));
    }

    public boolean isRefreshToken(Claims claims) {
        return "refresh".equals(claims.get("typ", String.class));
    }

    private String createToken(Long userId, String sessionId, String tokenType, long ttlSeconds) {
        Instant now = Instant.now();

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("sid", sessionId)
                .claim("typ", tokenType)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(ttlSeconds)))
                .signWith(secretKey)
                .compact();
    }
}

