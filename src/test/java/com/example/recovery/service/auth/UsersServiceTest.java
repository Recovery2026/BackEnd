package com.example.recovery.service.auth;

import com.example.recovery.common.exception.EmailAlreadyExistException;
import com.example.recovery.domain.user.UserCredential;
import com.example.recovery.domain.user.Users;
import com.example.recovery.repository.users.UserCredentialRepository;
import com.example.recovery.repository.users.UsersRepository;
import com.example.recovery.request.auth.SignupRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsersService 테스트")
class UsersServiceTest {

    @Mock
    private UserCredentialRepository userCredentialRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UsersRepository usersRepository;

    @InjectMocks
    private UsersService usersService;

    private SignupRequest signupRequest;

    @BeforeEach
    void setUp() {
        signupRequest = new SignupRequest();
        signupRequest.setEmail("test@example.com");
        signupRequest.setPassword("password123");
        signupRequest.setNickname("testUser");
    }

    @Test
    @DisplayName("성공: 회원가입 시 새로운 사용자 생성")
    void signup_success() {
        // given
        when(userCredentialRepository.findByEmail(signupRequest.getEmail()))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(signupRequest.getPassword()))
                .thenReturn("encoded_password");

        // when
        usersService.signup(signupRequest);

        // then
        verify(usersRepository, times(1)).save(any(Users.class));
        verify(userCredentialRepository, times(1)).save(any(UserCredential.class));
    }

    @Test
    @DisplayName("성공: 회원가입 시 Users 엔티티에 올바른 닉네임 설정")
    void signup_success_user_nickname_set() {
        // given
        when(userCredentialRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString()))
                .thenReturn("encoded_password");

        ArgumentCaptor<Users> usersCaptor = ArgumentCaptor.forClass(Users.class);

        // when
        usersService.signup(signupRequest);

        // then
        verify(usersRepository).save(usersCaptor.capture());
        Users savedUser = usersCaptor.getValue();
        assertEquals("testUser", savedUser.getNickname());
    }

    @Test
    @DisplayName("성공: 회원가입 시 UserCredential 엔티티에 올바른 이메일과 암호화된 비밀번호 설정")
    void signup_success_credential_saved_correctly() {
        // given
        when(userCredentialRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123"))
                .thenReturn("encoded_password_hash");

        ArgumentCaptor<UserCredential> credentialCaptor = ArgumentCaptor.forClass(UserCredential.class);

        // when
        usersService.signup(signupRequest);

        // then
        verify(userCredentialRepository).save(credentialCaptor.capture());
        UserCredential savedCredential = credentialCaptor.getValue();
        assertEquals("test@example.com", savedCredential.getEmail());
        assertEquals("encoded_password_hash", savedCredential.getPassword());
    }

    @Test
    @DisplayName("실패: 이미 존재하는 이메일로 회원가입하면 EmailAlreadyExistException 발생")
    void signup_fail_duplicate_email() {
        // given
        UserCredential existingCredential = new UserCredential();
        when(userCredentialRepository.findByEmail(signupRequest.getEmail()))
                .thenReturn(Optional.of(existingCredential));

        // when & then
        assertThrows(EmailAlreadyExistException.class, () -> {
            usersService.signup(signupRequest);
        });

        verify(usersRepository, never()).save(any());
        verify(userCredentialRepository, never()).save(any(UserCredential.class));
    }

    @Test
    @DisplayName("실패: 이미 존재하는 이메일로 회원가입할 때 올바른 예외 메시지 반환")
    void signup_fail_duplicate_email_message() {
        // given
        UserCredential existingCredential = new UserCredential();
        when(userCredentialRepository.findByEmail(signupRequest.getEmail()))
                .thenReturn(Optional.of(existingCredential));

        // when & then
        EmailAlreadyExistException exception = assertThrows(EmailAlreadyExistException.class, () -> {
            usersService.signup(signupRequest);
        });

        assertEquals("이미 존재하는 이메일입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("성공: 비밀번호 암호화")
    void signup_success_password_encoded() {
        // given
        when(userCredentialRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode("password123"))
                .thenReturn("hashed_password");

        // when
        usersService.signup(signupRequest);

        // then
        verify(passwordEncoder, times(1)).encode("password123");
    }

    @Test
    @DisplayName("성공: Users와 UserCredential 연결된다")
    void signup_success_user_credential_relationship() {
        // given
        when(userCredentialRepository.findByEmail(anyString()))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString()))
                .thenReturn("encoded_password");

        ArgumentCaptor<Users> usersCaptor = ArgumentCaptor.forClass(Users.class);
        ArgumentCaptor<UserCredential> credentialCaptor = ArgumentCaptor.forClass(UserCredential.class);

        // when
        usersService.signup(signupRequest);

        // then
        verify(usersRepository).save(usersCaptor.capture());
        verify(userCredentialRepository).save(credentialCaptor.capture());

        Users savedUser = usersCaptor.getValue();
        UserCredential savedCredential = credentialCaptor.getValue();

        assertNotNull(savedCredential.getUsers());
        assertEquals(savedUser, savedCredential.getUsers());
    }
}

