package com.example.recovery.service.auth;

import com.example.recovery.domain.user.UserCredential;
import com.example.recovery.repository.users.UserCredentialRepository;
import com.example.recovery.request.auth.LoginRequest;
import com.example.recovery.request.auth.RefreshTokenRequest;
import com.example.recovery.response.auth.TokenResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthTokenService {
    private static final String SESSION_KEY_PREFIX = "auth:session:";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final String REFRESH_TOKEN_PATH = "/api/auth";

    private final UserCredentialRepository userCredentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final StringRedisTemplate stringRedisTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    public TokenResponse login(LoginRequest request) {
        UserCredential credential = userCredentialRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다."));

        if (!passwordEncoder.matches(request.getPassword(), credential.getPassword())) {
            throw new BadCredentialsException("이메일 또는 비밀번호가 올바르지 않습니다.");
        }

        Long userId = credential.getUsers().getId();
        String sessionId = UUID.randomUUID().toString();

        String accessToken = jwtTokenProvider.createAccessToken(userId, sessionId);
        String refreshToken = jwtTokenProvider.createRefreshToken(userId, sessionId);

        saveSession(userId, sessionId, refreshToken);

        return new TokenResponse("Bearer", accessToken, refreshToken, jwtTokenProvider.getAccessTokenSeconds());
    }

    public String buildRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, refreshToken)
                .httpOnly(true)
                .secure(true)
                .path(REFRESH_TOKEN_PATH)
                .sameSite("Lax")
                .maxAge(jwtTokenProvider.getRefreshTokenSeconds())
                .build()
                .toString();
    }

    public String buildRefreshTokenClearCookie() {
        return ResponseCookie.from(REFRESH_TOKEN_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(true)
                .path(REFRESH_TOKEN_PATH)
                .sameSite("Lax")
                .maxAge(0)
                .build()
                .toString();
    }

    public TokenResponse refresh(RefreshTokenRequest request) {
        Claims claims;
        try {
            claims = jwtTokenProvider.parse(request.getRefreshToken());
        } catch (JwtException | IllegalArgumentException ex) {
            throw new BadCredentialsException("유효하지 않은 refresh token입니다.", ex);
        }

        if (!jwtTokenProvider.isRefreshToken(claims)) {
            throw new BadCredentialsException("refresh token 타입이 아닙니다.");
        }

        Long userId = jwtTokenProvider.extractUserId(claims);
        String sessionId = jwtTokenProvider.extractSessionId(claims);

        String key = sessionKey(userId, sessionId);
        String savedRefreshToken = stringRedisTemplate.opsForValue().get(key);
        if (savedRefreshToken == null || !savedRefreshToken.equals(request.getRefreshToken())) {
            throw new BadCredentialsException("세션이 만료되었거나 이미 로그아웃되었습니다.");
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(userId, sessionId);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(userId, sessionId);
        saveSession(userId, sessionId, newRefreshToken);

        return new TokenResponse("Bearer", newAccessToken, newRefreshToken, jwtTokenProvider.getAccessTokenSeconds());
    }

    public TokenResponse refreshWithCookie(String refreshToken) {
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken(refreshToken);
        return refresh(request);
    }

    public void logout(String accessToken) {
        Claims claims = jwtTokenProvider.parse(accessToken);
        if (!jwtTokenProvider.isAccessToken(claims)) {
            throw new BadCredentialsException("access token 타입이 아닙니다.");
        }

        Long userId = jwtTokenProvider.extractUserId(claims);
        String sessionId = jwtTokenProvider.extractSessionId(claims);
        stringRedisTemplate.delete(sessionKey(userId, sessionId));
    }

    public Optional<Long> validateAccessToken(String accessToken) {
        Claims claims;
        try {
            claims = jwtTokenProvider.parse(accessToken);
        } catch (JwtException | IllegalArgumentException ex) {
            return Optional.empty();
        }

        if (!jwtTokenProvider.isAccessToken(claims)) {
            return Optional.empty();
        }

        Long userId = jwtTokenProvider.extractUserId(claims);
        String sessionId = jwtTokenProvider.extractSessionId(claims);
        String key = sessionKey(userId, sessionId);

        if (!Boolean.TRUE.equals(stringRedisTemplate.hasKey(key))) {
            return Optional.empty();
        }

        return Optional.of(userId);
    }

    private void saveSession(Long userId, String sessionId, String refreshToken) {
        stringRedisTemplate.opsForValue().set(
                sessionKey(userId, sessionId),
                refreshToken,
                jwtTokenProvider.getRefreshTokenSeconds(),
                TimeUnit.SECONDS
        );
    }

    private String sessionKey(Long userId, String sessionId) {
        return SESSION_KEY_PREFIX + userId + ":" + sessionId;
    }

    public Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증되지 않은 요청입니다.");
        }

        return userId;
    }
}

