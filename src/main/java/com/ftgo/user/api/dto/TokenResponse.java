package com.ftgo.user.api.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class TokenResponse {

    private String token;
    private String refreshToken;

    public TokenResponse(String token, String refreshToken) {
        this.token = token;
        this.refreshToken = refreshToken;
    }
}
