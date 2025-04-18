package com.ftgo.user.service;

import com.ftgo.user.api.dto.LoginRequest;
import com.ftgo.user.api.dto.TokenResponse;
import com.ftgo.user.config.security.config.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public TokenResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        String token = tokenProvider.generateToken(
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal());
        return new TokenResponse(token);
    }
}
