package com.ftgo.user.api.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class TokenResponse {

    private String token;
    private String refreshToken;

    public TokenResponse(String token) {
        this.token = token;
    }
}
