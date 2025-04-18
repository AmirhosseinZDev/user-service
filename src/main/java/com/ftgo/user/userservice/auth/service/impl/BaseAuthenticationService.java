package com.ftgo.user.userservice.auth.service.impl;

import com.ftgo.user.userservice.auth.dto.LoginRequest;
import com.ftgo.user.userservice.auth.dto.TokenResponse;
import com.ftgo.user.userservice.security.config.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class BaseAuthenticationService
        implements com.ftgo.user.userservice.auth.service.api.AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    @Override
    public TokenResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        String token = tokenProvider.generateToken((org.springframework.security.core.userdetails.User) authentication.getPrincipal());

        return new TokenResponse(token);
    }

}
