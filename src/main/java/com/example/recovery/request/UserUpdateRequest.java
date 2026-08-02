package com.example.recovery.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {
    @NotBlank(message = "password는 필수입니다.")
    private String password;

    @NotBlank(message = "nickname은 필수입니다.")
    private String nickname;
}
