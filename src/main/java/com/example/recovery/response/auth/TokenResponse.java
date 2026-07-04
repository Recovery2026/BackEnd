package com.example.recovery.response.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TokenResponse {
    private final String tokenType;
    private final String accessToken;
    @JsonIgnore
    private final String refreshToken;
    private final long expiresIn;
}

