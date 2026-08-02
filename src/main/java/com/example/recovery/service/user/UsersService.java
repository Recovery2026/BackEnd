package com.example.recovery.service.user;

import com.example.recovery.common.exception.EmailAlreadyExistException;
import com.example.recovery.common.exception.UsersNotFoundException;
import com.example.recovery.domain.user.UserCredential;
import com.example.recovery.domain.user.Users;
import com.example.recovery.repository.users.UserCredentialRepository;
import com.example.recovery.repository.users.UsersRepository;
import com.example.recovery.request.UserPasswordUpdateRequest;
import com.example.recovery.request.UserUpdateRequest;
import com.example.recovery.request.auth.SignupRequest;
import com.example.recovery.response.UserResponse;
import com.example.recovery.service.auth.AuthTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.InvalidMediaTypeException;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsersService {
    private final UserCredentialRepository userCredentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final UsersRepository usersRepository;
    private final AuthTokenService authTokenService;

    @Value("${app.supabase.url:}")
    private String supabaseUrl;

    @Value("${app.supabase.service-role-key:}")
    private String supabaseServiceRoleKey;

    @Value("${app.supabase.profile-bucket:profileImage}")
    private String supabaseProfileBucket;

    public void signup(SignupRequest request) {
        if (userCredentialRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new EmailAlreadyExistException("이미 존재하는 이메일입니다.");
        }

        UserCredential credential = new UserCredential();
        Users users = new Users();
        users.setNickname(request.getNickname());

        credential.setEmail(request.getEmail());
        credential.setPassword(passwordEncoder.encode(request.getPassword()));
        credential.setUsers(users);

        usersRepository.save(users);
        userCredentialRepository.save(credential);
    }

    @Transactional(readOnly = true)
    public UserResponse getUser() {
        Long userId = authTokenService.getCurrentUserId();

        UserCredential credential = userCredentialRepository.findByUsersId(userId)
                .orElseThrow(() -> new UsersNotFoundException("해당 사용자가 없습니다."));

        Users users = credential.getUsers();

        return UserResponse.builder()
                .email(credential.getEmail())
                .nickname(users.getNickname())
                .profileUrl(users.getProfileUrl())
                .build();
    }

    @Transactional
    public void updateUser(UserUpdateRequest request) {
        Long userId = authTokenService.getCurrentUserId();

        UserCredential credential = userCredentialRepository.findByUsersId(userId)
                .orElseThrow(() -> new UsersNotFoundException("해당 사용자가 없습니다."));

        if (!passwordEncoder.matches(request.getPassword(), credential.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        credential.getUsers().setNickname(request.getNickname());
        credential.setUpdatedAt(OffsetDateTime.now());
    }

    @Transactional
    public void updatePassword(UserPasswordUpdateRequest request) {
        Long userId = authTokenService.getCurrentUserId();

        UserCredential credential = userCredentialRepository.findByUsersId(userId)
                .orElseThrow(() -> new UsersNotFoundException("해당 사용자가 없습니다."));

        if (!passwordEncoder.matches(request.getCurrentPassword(), credential.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }

        credential.setPassword(passwordEncoder.encode(request.getNewPassword()));
        credential.setUpdatedAt(OffsetDateTime.now());
    }

    @Transactional
    public void updateProfileImg(MultipartFile multipartFile) {
        if (StringUtils.isBlank(supabaseUrl) || StringUtils.isBlank(supabaseServiceRoleKey)) {
            throw new IllegalStateException("Supabase 설정이 누락되었습니다.");
        }

        Long userId = authTokenService.getCurrentUserId();

        UserCredential credential = userCredentialRepository.findByUsersId(userId)
                .orElseThrow(() -> new UsersNotFoundException("해당 사용자가 없습니다."));

        String originalFilename = multipartFile.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf('.'));
        }

        String objectPath = userId + extension;
        String encodedObjectPath = UriUtils.encodePath(
                objectPath,
                StandardCharsets.UTF_8
        );
        String normalizedBaseUrl = supabaseUrl.endsWith("/")
                ? supabaseUrl.substring(0, supabaseUrl.length() - 1)
                : supabaseUrl;
        String uploadUrl = normalizedBaseUrl
                + "/storage/v1/object/"
                + supabaseProfileBucket
                + "/"
                + encodedObjectPath;

        try {
            MediaType contentType = resolveContentTypeOrDefault(multipartFile.getContentType());

            RestClient.create().post()
                    .uri(uploadUrl)
                    .header("apikey", supabaseServiceRoleKey)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + supabaseServiceRoleKey)
                    .header("x-upsert", "true")
                    .contentType(contentType)
                    .body(multipartFile.getBytes())
                    .retrieve()
                    .toBodilessEntity();
        } catch (IOException | RestClientResponseException e) {
            throw new IllegalStateException("프로필 이미지 저장에 실패했습니다.", e);
        }

        String publicUrl = normalizedBaseUrl
                + "/storage/v1/object/public/"
                + supabaseProfileBucket
                + "/"
                + encodedObjectPath;

        credential.getUsers().setProfileUrl(publicUrl);
        credential.setUpdatedAt(OffsetDateTime.now());
    }

    private MediaType resolveContentTypeOrDefault(String rawContentType) {
        if (StringUtils.isBlank(rawContentType)) {
            log.warn("업로드 파일 Content-Type이 비어 있어 기본 타입(application/octet-stream)으로 처리합니다.");
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        try {
            return MediaType.parseMediaType(rawContentType);
        } catch (InvalidMediaTypeException e) {
            log.warn("잘못된 Content-Type '{}' 이 전달되어 기본 타입(application/octet-stream)으로 처리합니다.", rawContentType);
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    @Transactional
    public void withdrawUser() {
        Long userId = authTokenService.getCurrentUserId();

        UserCredential credential = userCredentialRepository.findByUsersId(userId)
                .orElseThrow(() -> new UsersNotFoundException("해당 사용자가 없습니다."));

        credential.getUsers().setActivation(false);
        credential.setUpdatedAt(OffsetDateTime.now());
    }
}
