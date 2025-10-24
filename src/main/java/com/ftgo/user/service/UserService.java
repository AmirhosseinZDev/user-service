package com.ftgo.user.service;

import com.ftgo.user.api.dto.LoginRequest;
import com.ftgo.user.api.dto.RefreshTokenRequest;
import com.ftgo.user.api.dto.RegisterUserRequestDto;
import com.ftgo.user.api.dto.TokenResponse;
import com.ftgo.user.api.dto.enumaration.UserRole;
import com.ftgo.user.api.exception.InvalidRefreshTokenException;
import com.ftgo.user.config.security.config.JwtTokenProvider;
import com.ftgo.user.persistence.document.AppUserDocument;
import com.ftgo.user.persistence.entity.enumaration.Role;
import com.ftgo.user.persistence.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class UserService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final AppUserRepository appUserRepository;

    public TokenResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
        
        String username = ((org.springframework.security.core.userdetails.User) authentication.getPrincipal()).getUsername();
        String token = tokenProvider.generateToken(
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal());
        String refreshToken = tokenProvider.generateRefreshToken(username);
        
        // Save refresh token to database
        Optional<AppUserDocument> userOpt = appUserRepository.findByUsername(username);
        userOpt.ifPresent(user -> {
            user.setRefreshToken(refreshToken);
            appUserRepository.save(user);
        });
        
        return new TokenResponse(token, refreshToken);
    }

    public void register(RegisterUserRequestDto requestDto) {
        AppUserDocument appUser = new AppUserDocument();
        appUser.setUsername(requestDto.getUsername());
        appUser.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        appUser.setEmail(requestDto.getEmail());
        appUser.setPhoneNumber(requestDto.getPhoneNumber());
        appUser.getRoles().add(convertTORole(requestDto.getRole()));
        appUserRepository.save(appUser);
    }

    public TokenResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        
        // Validate the refresh token
        String username = tokenProvider.validateRefreshToken(refreshToken);
        if (username == null) {
            throw new InvalidRefreshTokenException("Invalid or expired refresh token");
        }
        
        // Verify the refresh token exists in the database and matches
        Optional<AppUserDocument> userOpt = appUserRepository.findByUsername(username);
        if (userOpt.isEmpty() || !refreshToken.equals(userOpt.get().getRefreshToken())) {
            throw new InvalidRefreshTokenException("Invalid or expired refresh token");
        }
        
        AppUserDocument user = userOpt.get();
        
        // Generate new access token
        UserDetails userDetails = createUserDetails(user);
        String newAccessToken = tokenProvider.generateToken(userDetails);
        
        // Generate new refresh token (token rotation for security)
        String newRefreshToken = tokenProvider.generateRefreshToken(username);
        user.setRefreshToken(newRefreshToken);
        appUserRepository.save(user);
        
        return new TokenResponse(newAccessToken, newRefreshToken);
    }

    private UserDetails createUserDetails(AppUserDocument user) {
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getRoles().stream()
                        .map(role -> (GrantedAuthority) () -> role.name())
                        .toList()
        );
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
