package com.ftgo.user.userservice.auth.service.api;

import com.ftgo.user.userservice.auth.dto.LoginRequest;
import com.ftgo.user.userservice.auth.dto.TokenResponse;

public interface AuthenticationService {

    TokenResponse login(LoginRequest loginRequest);
}
