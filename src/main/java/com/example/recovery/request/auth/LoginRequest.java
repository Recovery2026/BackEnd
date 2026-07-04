package com.example.recovery.request.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    @NotBlank(message = "email은 필수입니다.")
    @Email(message = "유효한 email 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "password는 필수입니다.")
    private String password;
}

