package com.ftgo.user.service;

import com.ftgo.user.api.dto.LoginRequest;
import com.ftgo.user.api.dto.RegisterUserRequestDto;
import com.ftgo.user.api.dto.TokenResponse;
import com.ftgo.user.api.dto.enumaration.UserRole;
import com.ftgo.user.config.security.config.JwtTokenProvider;
import com.ftgo.user.persistence.entity.User;
import com.ftgo.user.persistence.entity.enumaration.Role;
import com.ftgo.user.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class UserService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public TokenResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        String token = tokenProvider.generateToken(
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal());
        return new TokenResponse(token);
    }

    public void register(RegisterUserRequestDto requestDto) {
        User user = new User();
        user.setUsername(requestDto.getUsername());
        user.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        user.setEmail(requestDto.getEmail());
        user.setPhoneNumber(requestDto.getPhoneNumber());
        user.setRole(convertTORole(requestDto.getRole()));
        userRepository.save(user);
    }

    private Role convertTORole(UserRole userRole) {
        return switch (userRole) {
            case ROLE_USER -> Role.ROLE_USER;
            case ROLE_ADMIN -> Role.ROLE_ADMIN;
            case ROLE_COURIER -> Role.ROLE_COURIER;
            case ROLE_RESTAURANT -> Role.ROLE_RESTAURANT;
        };
    }
}
