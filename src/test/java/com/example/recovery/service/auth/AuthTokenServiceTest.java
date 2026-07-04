package com.example.recovery.service.auth;

import com.example.recovery.domain.user.UserCredential;
import com.example.recovery.domain.user.Users;
import com.example.recovery.repository.users.UserCredentialRepository;
import com.example.recovery.request.auth.LoginRequest;
import com.example.recovery.request.auth.RefreshTokenRequest;
import com.example.recovery.response.auth.TokenResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuthTokenService 테스트")
class AuthTokenServiceTest {

    @Mock
    private UserCredentialRepository userCredentialRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private AuthTokenService authTokenService;

    private UserCredential userCredential;
    private Users users;
    private LoginRequest loginRequest;

    @BeforeEach
    void setUp() {
        users = new Users();
        users.setId(1L);
        users.setNickname("testUser");

        userCredential = new UserCredential();
        userCredential.setEmail("test@example.com");
        userCredential.setPassword("encoded_password");
        userCredential.setUsers(users);

        loginRequest = new LoginRequest();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password123");
    }

    @Test
    @DisplayName("성공: 올바른 이메일과 비밀번호로 로그인 시 TokenResponse 반환")
    void login_success() {
        // given
        when(userCredentialRepository.findByEmail(loginRequest.getEmail()))
                .thenReturn(Optional.of(userCredential));
        when(passwordEncoder.matches(loginRequest.getPassword(), userCredential.getPassword()))
                .thenReturn(true);
        when(jwtTokenProvider.createAccessToken(anyLong(), anyString()))
                .thenReturn("access_token");
        when(jwtTokenProvider.createRefreshToken(anyLong(), anyString()))
                .thenReturn("refresh_token");
        when(jwtTokenProvider.getAccessTokenSeconds())
                .thenReturn(300L);

        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);

        // when
        TokenResponse response = authTokenService.login(loginRequest);

        // then
        assertNotNull(response);
        assertEquals("Bearer", response.getTokenType());
        assertEquals("access_token", response.getAccessToken());
        assertEquals("refresh_token", response.getRefreshToken());
        assertEquals(300L, response.getExpiresIn());
    }

    @Test
    @DisplayName("성공: 로그인 시 Redis에 세션 저장")
    void login_success_session_saved_to_redis() {
        // given
        when(userCredentialRepository.findByEmail(loginRequest.getEmail()))
                .thenReturn(Optional.of(userCredential));
        when(passwordEncoder.matches(loginRequest.getPassword(), userCredential.getPassword()))
                .thenReturn(true);
        when(jwtTokenProvider.createAccessToken(anyLong(), anyString()))
                .thenReturn("access_token");
        when(jwtTokenProvider.createRefreshToken(anyLong(), anyString()))
                .thenReturn("refresh_token");
        when(jwtTokenProvider.getAccessTokenSeconds())
                .thenReturn(300L);
        when(jwtTokenProvider.getRefreshTokenSeconds())
                .thenReturn(1200L);

        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);

        // when
        authTokenService.login(loginRequest);

        // then
        verify(valueOps, times(1)).set(anyString(), eq("refresh_token"), eq(1200L), any());
    }

    @Test
    @DisplayName("실패: 존재하지 않는 이메일로 로그인 시 BadCredentialsException 발생")
    void login_fail_email_not_found() {
        // given
        when(userCredentialRepository.findByEmail(loginRequest.getEmail()))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(BadCredentialsException.class, () -> {
            authTokenService.login(loginRequest);
        });
    }

    @Test
    @DisplayName("실패: 잘못된 비밀번호로 로그인 시 BadCredentialsException 발생")
    void login_fail_wrong_password() {
        // given
        when(userCredentialRepository.findByEmail(loginRequest.getEmail()))
                .thenReturn(Optional.of(userCredential));
        when(passwordEncoder.matches(loginRequest.getPassword(), userCredential.getPassword()))
                .thenReturn(false);

        // when & then
        assertThrows(BadCredentialsException.class, () -> {
            authTokenService.login(loginRequest);
        });
    }

    @Test
    @DisplayName("실패: 로그인 실패 시 올바른 예외 메시지 반환")
    void login_fail_exception_message() {
        // given
        when(userCredentialRepository.findByEmail(loginRequest.getEmail()))
                .thenReturn(Optional.empty());

        // when & then
        BadCredentialsException exception = assertThrows(BadCredentialsException.class, () -> {
            authTokenService.login(loginRequest);
        });

        assertEquals("이메일 또는 비밀번호가 올바르지 않습니다.", exception.getMessage());
    }

    @Test
    @DisplayName("성공: 유효한 refresh token으로 새로운 토큰 발급")
    void refresh_success() {
        // given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("valid_refresh_token");

        Claims claims = mock(Claims.class);
        when(jwtTokenProvider.parse("valid_refresh_token")).thenReturn(claims);
        when(jwtTokenProvider.isRefreshToken(claims)).thenReturn(true);
        when(jwtTokenProvider.extractUserId(claims)).thenReturn(1L);
        when(jwtTokenProvider.extractSessionId(claims)).thenReturn("session_1");

        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("auth:session:1:session_1")).thenReturn("valid_refresh_token");

        when(jwtTokenProvider.createAccessToken(1L, "session_1")).thenReturn("new_access_token");
        when(jwtTokenProvider.createRefreshToken(1L, "session_1")).thenReturn("new_refresh_token");
        when(jwtTokenProvider.getAccessTokenSeconds()).thenReturn(300L);
        when(jwtTokenProvider.getRefreshTokenSeconds()).thenReturn(1200L);

        // when
        TokenResponse response = authTokenService.refresh(request);

        // then
        assertNotNull(response);
        assertEquals("Bearer", response.getTokenType());
        assertEquals("new_access_token", response.getAccessToken());
        assertEquals("new_refresh_token", response.getRefreshToken());
    }

    @Test
    @DisplayName("실패: 유효하지 않은 refresh token으로 요청 시 BadCredentialsException 발생")
    void refresh_fail_invalid_token() {
        // given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("invalid_token");

        when(jwtTokenProvider.parse("invalid_token")).thenThrow(new JwtException("Invalid token"));

        // when & then
        assertThrows(BadCredentialsException.class, () -> {
            authTokenService.refresh(request);
        });
    }

    @Test
    @DisplayName("실패: refresh token이 아닌 access token으로 refresh 요청 시  BadCredentialsException 발생한다")
    void refresh_fail_not_refresh_token() {
        // given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("access_token_string");

        Claims claims = mock(Claims.class);
        when(jwtTokenProvider.parse("access_token_string")).thenReturn(claims);
        when(jwtTokenProvider.isRefreshToken(claims)).thenReturn(false);

        // when & then
        assertThrows(BadCredentialsException.class, () -> {
            authTokenService.refresh(request);
        });
    }

    @Test
    @DisplayName("실패: Redis에 저장된 token과 요청된 token이 일치하지 않으면 BadCredentialsException 발생")
    void refresh_fail_token_mismatch() {
        // given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("token_from_request");

        Claims claims = mock(Claims.class);
        when(jwtTokenProvider.parse("token_from_request")).thenReturn(claims);
        when(jwtTokenProvider.isRefreshToken(claims)).thenReturn(true);
        when(jwtTokenProvider.extractUserId(claims)).thenReturn(1L);
        when(jwtTokenProvider.extractSessionId(claims)).thenReturn("session_1");

        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("auth:session:1:session_1")).thenReturn("different_token_in_redis");

        // when & then
        assertThrows(BadCredentialsException.class, () -> {
            authTokenService.refresh(request);
        });
    }

    @Test
    @DisplayName("실패: Redis에 저장된 session이 만료되면 BadCredentialsException 발생")
    void refresh_fail_session_expired() {
        // given
        RefreshTokenRequest request = new RefreshTokenRequest();
        request.setRefreshToken("token_from_request");

        Claims claims = mock(Claims.class);
        when(jwtTokenProvider.parse("token_from_request")).thenReturn(claims);
        when(jwtTokenProvider.isRefreshToken(claims)).thenReturn(true);
        when(jwtTokenProvider.extractUserId(claims)).thenReturn(1L);
        when(jwtTokenProvider.extractSessionId(claims)).thenReturn("session_1");

        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("auth:session:1:session_1")).thenReturn(null);

        // when & then
        assertThrows(BadCredentialsException.class, () -> {
            authTokenService.refresh(request);
        });
    }

    @Test
    @DisplayName("성공: access token으로 로그아웃 시 Redis에서 세션 삭제")
    void logout_success() {
        // given
        String accessToken = "valid_access_token";
        Claims claims = mock(Claims.class);

        when(jwtTokenProvider.parse(accessToken)).thenReturn(claims);
        when(jwtTokenProvider.isAccessToken(claims)).thenReturn(true);
        when(jwtTokenProvider.extractUserId(claims)).thenReturn(1L);
        when(jwtTokenProvider.extractSessionId(claims)).thenReturn("session_1");

        // when
        authTokenService.logout(accessToken);

        // then
        verify(stringRedisTemplate, times(1)).delete("auth:session:1:session_1");
    }

    @Test
    @DisplayName("실패: access token이 아닌 토큰으로 로그아웃 시도하면 BadCredentialsException 발생")
    void logout_fail_not_access_token() {
        // given
        String refreshToken = "refresh_token_string";
        Claims claims = mock(Claims.class);

        when(jwtTokenProvider.parse(refreshToken)).thenReturn(claims);
        when(jwtTokenProvider.isAccessToken(claims)).thenReturn(false);

        // when & then
        assertThrows(BadCredentialsException.class, () -> {
            authTokenService.logout(refreshToken);
        });
    }

    @Test
    @DisplayName("성공: 유효한 access token을 검증하면 userId 반환")
    void validateAccessToken_success() {
        // given
        String accessToken = "valid_access_token";
        Claims claims = mock(Claims.class);

        when(jwtTokenProvider.parse(accessToken)).thenReturn(claims);
        when(jwtTokenProvider.isAccessToken(claims)).thenReturn(true);
        when(jwtTokenProvider.extractUserId(claims)).thenReturn(1L);
        when(jwtTokenProvider.extractSessionId(claims)).thenReturn("session_1");

        when(stringRedisTemplate.hasKey("auth:session:1:session_1")).thenReturn(true);

        // when
        Optional<Long> result = authTokenService.validateAccessToken(accessToken);

        // then
        assertTrue(result.isPresent());
        assertEquals(1L, result.get());
    }

    @Test
    @DisplayName("실패: 유효하지 않은 access token을 검증하면 Optional.empty 반환")
    void validateAccessToken_fail_invalid_token() {
        // given
        String invalidToken = "invalid_token";

        when(jwtTokenProvider.parse(invalidToken)).thenThrow(new JwtException("Invalid token"));

        // when
        Optional<Long> result = authTokenService.validateAccessToken(invalidToken);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("실패: access token이 아닌 토큰을 검증하면 Optional.empty 반환")
    void validateAccessToken_fail_not_access_token() {
        // given
        String refreshToken = "refresh_token_string";
        Claims claims = mock(Claims.class);

        when(jwtTokenProvider.parse(refreshToken)).thenReturn(claims);
        when(jwtTokenProvider.isAccessToken(claims)).thenReturn(false);

        // when
        Optional<Long> result = authTokenService.validateAccessToken(refreshToken);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("실패: Redis에서 세션을 찾을 수 없으면 Optional.empty 반환")
    void validateAccessToken_fail_session_not_found() {
        // given
        String accessToken = "valid_token_but_no_session";
        Claims claims = mock(Claims.class);

        when(jwtTokenProvider.parse(accessToken)).thenReturn(claims);
        when(jwtTokenProvider.isAccessToken(claims)).thenReturn(true);
        when(jwtTokenProvider.extractUserId(claims)).thenReturn(1L);
        when(jwtTokenProvider.extractSessionId(claims)).thenReturn("session_1");

        when(stringRedisTemplate.hasKey("auth:session:1:session_1")).thenReturn(false);

        // when
        Optional<Long> result = authTokenService.validateAccessToken(accessToken);

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("성공: refresh token 쿠키가 올바르게 생성")
    void buildRefreshTokenCookie_success() {
        // given
        when(jwtTokenProvider.getRefreshTokenSeconds()).thenReturn(1200L);

        // when
        String cookie = authTokenService.buildRefreshTokenCookie("test_refresh_token");

        // then
        assertNotNull(cookie);
        assertTrue(cookie.contains("refreshToken=test_refresh_token"));
        assertTrue(cookie.contains("Path=/api/auth"));
        assertTrue(cookie.contains("HttpOnly"));
        assertTrue(cookie.contains("Secure"));
        assertTrue(cookie.contains("SameSite=Lax"));
    }

    @Test
    @DisplayName("성공: refresh token 쿠키 삭제 요청이 올바르게 생성")
    void buildRefreshTokenClearCookie_success() {
        // given & when
        String cookie = authTokenService.buildRefreshTokenClearCookie();

        // then
        assertNotNull(cookie);
        assertTrue(cookie.contains("refreshToken="));
        assertTrue(cookie.contains("Max-Age=0"));
        assertTrue(cookie.contains("HttpOnly"));
        assertTrue(cookie.contains("Secure"));
        assertTrue(cookie.contains("SameSite=Lax"));
    }

    @Test
    @DisplayName("성공: 쿠키에서 refresh token으로 새로운 토큰 발급")
    void refreshWithCookie_success() {
        // given
        String cookieRefreshToken = "cookie_refresh_token";
        Claims claims = mock(Claims.class);

        when(jwtTokenProvider.parse(cookieRefreshToken)).thenReturn(claims);
        when(jwtTokenProvider.isRefreshToken(claims)).thenReturn(true);
        when(jwtTokenProvider.extractUserId(claims)).thenReturn(1L);
        when(jwtTokenProvider.extractSessionId(claims)).thenReturn("session_1");

        ValueOperations<String, String> valueOps = mock(ValueOperations.class);
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("auth:session:1:session_1")).thenReturn(cookieRefreshToken);

        when(jwtTokenProvider.createAccessToken(1L, "session_1")).thenReturn("new_access_token");
        when(jwtTokenProvider.createRefreshToken(1L, "session_1")).thenReturn("new_refresh_token");
        when(jwtTokenProvider.getAccessTokenSeconds()).thenReturn(300L);
        when(jwtTokenProvider.getRefreshTokenSeconds()).thenReturn(1200L);

        // when
        TokenResponse response = authTokenService.refreshWithCookie(cookieRefreshToken);

        // then
        assertNotNull(response);
        assertEquals("Bearer", response.getTokenType());
        assertEquals("new_access_token", response.getAccessToken());
    }

    @Test
    @DisplayName("성공: 현재 인증된 사용자의 ID를 가져올 수 있다")
    void getCurrentUserId_success() {
        // given
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(1L);

        // when
        Long userId = authTokenService.getCurrentUserId();

        // then
        assertEquals(1L, userId);
    }

    @Test
    @DisplayName("실패: 인증되지 않은 요청에서 사용자 ID를 가져오려면 ResponseStatusException이 발생한다")
    void getCurrentUserId_fail_not_authenticated() {
        // given
        SecurityContext securityContext = mock(SecurityContext.class);
        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(null);

        // when & then
        assertThrows(ResponseStatusException.class, () -> {
            authTokenService.getCurrentUserId();
        });
    }

    @Test
    @DisplayName("실패: principal이 Long이 아닌 경우 ResponseStatusException 발생")
    void getCurrentUserId_fail_invalid_principal_type() {
        // given
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn("invalid_principal");

        // when & then
        assertThrows(ResponseStatusException.class, () -> {
            authTokenService.getCurrentUserId();
        });
    }
}



