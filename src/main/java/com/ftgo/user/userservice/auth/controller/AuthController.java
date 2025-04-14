package com.ftgo.user.userservice.auth.controller;

import com.ftgo.user.userservice.auth.dto.LoginRequest;
import com.ftgo.user.userservice.auth.dto.TokenResponse;
import com.ftgo.user.userservice.security.config.JwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;

    public AuthController(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
    }

    @PostMapping("/login")
    public TokenResponse authenticateUser(@RequestBody LoginRequest loginRequest) {
        try {
            log.info("Authenticating user with username: {}", loginRequest.getUsername());
            // Authenticate the user using credentials
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            // Generate a JWT token after successful authentication
            String token = tokenProvider.generateToken((org.springframework.security.core.userdetails.User) authentication.getPrincipal());

            return new TokenResponse(token);
        } catch (AuthenticationException e) {
            log.error("Authentication failed for user: {}", loginRequest.getUsername(), e);
            throw new RuntimeException("Authentication failed", e);
        } catch (Exception e) {
            log.error("An unexpected error occurred during authentication", e);
            throw new RuntimeException("An unexpected error occurred", e);
        } finally {
            log.info("Authentication process completed for user: {}", loginRequest.getUsername());
        }
    }
}

