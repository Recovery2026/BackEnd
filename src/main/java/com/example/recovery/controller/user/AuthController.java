package com.example.recovery.controller.user;

import com.example.recovery.request.auth.LoginRequest;
import com.example.recovery.request.auth.RefreshTokenRequest;
import com.example.recovery.request.auth.SignupRequest;
import com.example.recovery.response.auth.MeResponse;
import com.example.recovery.response.auth.TokenResponse;
import com.example.recovery.service.auth.AuthTokenService;
import com.example.recovery.service.user.UsersService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthTokenService authTokenService;
    private final UsersService usersService;

    @PostMapping("/signup")
    @ResponseStatus(HttpStatus.CREATED)
    public void signup(@Valid @RequestBody SignupRequest request) {
        usersService.signup(request);
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse tokenResponse = authTokenService.login(request);

        // react에서 refresh요청을 보낼 때 쿠키값을 함께 보내야 함
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authTokenService.buildRefreshTokenCookie(tokenResponse.getRefreshToken()))
                .body(tokenResponse);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(
            @CookieValue(value = "refreshToken", required = false) String refreshTokenCookie,
            @Valid @RequestBody(required = false) RefreshTokenRequest request
    ) {
        String refreshToken = refreshTokenCookie != null ? refreshTokenCookie : (request != null ? request.getRefreshToken() : null);
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "refresh token이 필요합니다.");
        }

        TokenResponse tokenResponse = authTokenService.refreshWithCookie(refreshToken);
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, authTokenService.buildRefreshTokenCookie(tokenResponse.getRefreshToken()))
                .body(tokenResponse);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authorizationHeader) {
        if (!authorizationHeader.startsWith(BEARER_PREFIX)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Authorization 헤더 형식이 잘못되었습니다.");
        }

        String accessToken = authorizationHeader.substring(BEARER_PREFIX.length());
        authTokenService.logout(accessToken);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, authTokenService.buildRefreshTokenClearCookie())
                .build();
    }

    @GetMapping("/me")
    public MeResponse me(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Long userId)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "인증되지 않은 요청입니다.");
        }

        return new MeResponse(userId);
    }
}

