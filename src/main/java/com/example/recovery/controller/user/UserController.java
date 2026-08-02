package com.example.recovery.controller.user;

import com.example.recovery.request.UserPasswordUpdateRequest;
import com.example.recovery.request.UserUpdateRequest;
import com.example.recovery.response.UserResponse;
import com.example.recovery.service.user.UsersService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UsersService usersService;

    @GetMapping
    public UserResponse getUser() {
        return usersService.getUser();
    }

    @PatchMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateUser(@Validated @RequestBody UserUpdateRequest userUpdateRequest) {
        usersService.updateUser(userUpdateRequest);
    }

    @PatchMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updatePassword(@Validated @RequestBody UserPasswordUpdateRequest userPasswordUpdateRequest) {
        usersService.updatePassword(userPasswordUpdateRequest);
    }

    @PatchMapping("/profileImg")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateProfileImg(@RequestPart("multipartFile") MultipartFile multipartFile) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        usersService.updateProfileImg(multipartFile);
    }

    @DeleteMapping("/withdrawal")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void withdrawUser() {
        usersService.withdrawUser();
    }
}
