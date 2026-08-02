package com.example.recovery.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserPasswordUpdateRequest {
    @NotBlank(message = "currentPassword는 필수입니다.")
    private String currentPassword;

    @NotBlank(message = "newPassword는 필수입니다.")
    private String newPassword;
}
