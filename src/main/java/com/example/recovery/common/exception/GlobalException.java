package com.example.recovery.common.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

@Slf4j
@ControllerAdvice
public class GlobalException {

    // 바인딩 과정에서 타입 불일치 또는 검증 실패
    @ExceptionHandler(BindException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<String> handleBindException(BindException e) {
        FieldError fieldError = e.getFieldError();
        String message = "입력값 오류: " + (fieldError != null ? fieldError.getDefaultMessage() : "잘못된 요청입니다.");
        return ResponseEntity.badRequest().body(message);
    }
}
