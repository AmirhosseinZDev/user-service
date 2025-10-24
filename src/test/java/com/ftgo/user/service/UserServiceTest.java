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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AppUserRepository appUserRepository;

    @InjectMocks
    private UserService userService;

    private static final String TEST_USERNAME = "testUser";
    private static final String TEST_PASSWORD = "testPassword";
    private static final String ACCESS_TOKEN = "access.token.jwt";
    private static final String REFRESH_TOKEN = "refresh.token.jwt";
    private static final String NEW_ACCESS_TOKEN = "new.access.token.jwt";
    private static final String NEW_REFRESH_TOKEN = "new.refresh.token.jwt";

    private AppUserDocument testUser;
    private UserDetails testUserDetails;
    private Authentication authentication;

    @BeforeEach
    void setUp() {
        testUser = new AppUserDocument();
        testUser.setId("123");
        testUser.setUsername(TEST_USERNAME);
        testUser.setPassword("encodedPassword");
        testUser.setEmail("test@example.com");
        testUser.setPhoneNumber("1234567890");
        Set<Role> roles = new HashSet<>();
        roles.add(Role.ROLE_USER);
        testUser.setRoles(roles);

        testUserDetails = new User(TEST_USERNAME, TEST_PASSWORD, 
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        
        authentication = mock(Authentication.class);
    }

    @Test
    void testLogin_WithValidCredentials_ShouldReturnTokens() {
        // Given
        LoginRequest loginRequest = new LoginRequest(TEST_USERNAME, TEST_PASSWORD);
        when(authentication.getPrincipal()).thenReturn(testUserDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(testUserDetails)).thenReturn(ACCESS_TOKEN);
        when(tokenProvider.generateRefreshToken(TEST_USERNAME)).thenReturn(REFRESH_TOKEN);
        when(appUserRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        when(appUserRepository.save(any(AppUserDocument.class))).thenReturn(testUser);

        // When
        TokenResponse response = userService.login(loginRequest);

        // Then
        assertNotNull(response);
        assertEquals(ACCESS_TOKEN, response.getToken());
        assertEquals(REFRESH_TOKEN, response.getRefreshToken());
        
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenProvider).generateToken(testUserDetails);
        verify(tokenProvider).generateRefreshToken(TEST_USERNAME);
        verify(appUserRepository).findByUsername(TEST_USERNAME);
        verify(appUserRepository).save(testUser);
    }

    @Test
    void testLogin_ShouldSaveRefreshTokenToDatabase() {
        // Given
        LoginRequest loginRequest = new LoginRequest(TEST_USERNAME, TEST_PASSWORD);
        when(authentication.getPrincipal()).thenReturn(testUserDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(testUserDetails)).thenReturn(ACCESS_TOKEN);
        when(tokenProvider.generateRefreshToken(TEST_USERNAME)).thenReturn(REFRESH_TOKEN);
        when(appUserRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        when(appUserRepository.save(any(AppUserDocument.class))).thenReturn(testUser);

        // When
        userService.login(loginRequest);

        // Then
        ArgumentCaptor<AppUserDocument> userCaptor = ArgumentCaptor.forClass(AppUserDocument.class);
        verify(appUserRepository).save(userCaptor.capture());
        assertEquals(REFRESH_TOKEN, userCaptor.getValue().getRefreshToken());
    }

    @Test
    void testLogin_WhenUserNotFound_ShouldStillReturnTokens() {
        // Given
        LoginRequest loginRequest = new LoginRequest(TEST_USERNAME, TEST_PASSWORD);
        when(authentication.getPrincipal()).thenReturn(testUserDetails);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(tokenProvider.generateToken(testUserDetails)).thenReturn(ACCESS_TOKEN);
        when(tokenProvider.generateRefreshToken(TEST_USERNAME)).thenReturn(REFRESH_TOKEN);
        when(appUserRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.empty());

        // When
        TokenResponse response = userService.login(loginRequest);

        // Then
        assertNotNull(response);
        assertEquals(ACCESS_TOKEN, response.getToken());
        assertEquals(REFRESH_TOKEN, response.getRefreshToken());
        verify(appUserRepository, never()).save(any());
    }

    @Test
    void testRegister_ShouldCreateUserWithEncodedPassword() {
        // Given
        RegisterUserRequestDto requestDto = new RegisterUserRequestDto();
        requestDto.setUsername(TEST_USERNAME);
        requestDto.setPassword(TEST_PASSWORD);
        requestDto.setEmail("test@example.com");
        requestDto.setPhoneNumber("1234567890");
        requestDto.setRole(UserRole.ROLE_USER);
        
        when(passwordEncoder.encode(TEST_PASSWORD)).thenReturn("encodedPassword");
        when(appUserRepository.save(any(AppUserDocument.class))).thenReturn(testUser);

        // When
        userService.register(requestDto);

        // Then
        ArgumentCaptor<AppUserDocument> userCaptor = ArgumentCaptor.forClass(AppUserDocument.class);
        verify(appUserRepository).save(userCaptor.capture());
        
        AppUserDocument savedUser = userCaptor.getValue();
        assertEquals(TEST_USERNAME, savedUser.getUsername());
        assertEquals("encodedPassword", savedUser.getPassword());
        assertEquals("test@example.com", savedUser.getEmail());
        assertEquals("1234567890", savedUser.getPhoneNumber());
        assertTrue(savedUser.getRoles().contains(Role.ROLE_USER));
    }

    @Test
    void testRefreshToken_WithValidToken_ShouldReturnNewTokens() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest(REFRESH_TOKEN);
        testUser.setRefreshToken(REFRESH_TOKEN);
        
        when(tokenProvider.validateRefreshToken(REFRESH_TOKEN)).thenReturn(TEST_USERNAME);
        when(appUserRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        when(tokenProvider.generateToken(any(UserDetails.class))).thenReturn(NEW_ACCESS_TOKEN);
        when(tokenProvider.generateRefreshToken(TEST_USERNAME)).thenReturn(NEW_REFRESH_TOKEN);
        when(appUserRepository.save(any(AppUserDocument.class))).thenReturn(testUser);

        // When
        TokenResponse response = userService.refreshToken(request);

        // Then
        assertNotNull(response);
        assertEquals(NEW_ACCESS_TOKEN, response.getToken());
        assertEquals(NEW_REFRESH_TOKEN, response.getRefreshToken());
        
        verify(tokenProvider).validateRefreshToken(REFRESH_TOKEN);
        verify(appUserRepository).findByUsername(TEST_USERNAME);
        verify(tokenProvider).generateToken(any(UserDetails.class));
        verify(tokenProvider).generateRefreshToken(TEST_USERNAME);
    }

    @Test
    void testRefreshToken_ShouldRotateRefreshToken() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest(REFRESH_TOKEN);
        testUser.setRefreshToken(REFRESH_TOKEN);
        
        when(tokenProvider.validateRefreshToken(REFRESH_TOKEN)).thenReturn(TEST_USERNAME);
        when(appUserRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));
        when(tokenProvider.generateToken(any(UserDetails.class))).thenReturn(NEW_ACCESS_TOKEN);
        when(tokenProvider.generateRefreshToken(TEST_USERNAME)).thenReturn(NEW_REFRESH_TOKEN);
        when(appUserRepository.save(any(AppUserDocument.class))).thenReturn(testUser);

        // When
        userService.refreshToken(request);

        // Then
        ArgumentCaptor<AppUserDocument> userCaptor = ArgumentCaptor.forClass(AppUserDocument.class);
        verify(appUserRepository).save(userCaptor.capture());
        assertEquals(NEW_REFRESH_TOKEN, userCaptor.getValue().getRefreshToken());
    }

    @Test
    void testRefreshToken_WithInvalidToken_ShouldThrowException() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest("invalid.token");
        when(tokenProvider.validateRefreshToken("invalid.token")).thenReturn(null);

        // When & Then
        InvalidRefreshTokenException exception = assertThrows(
                InvalidRefreshTokenException.class,
                () -> userService.refreshToken(request)
        );
        
        assertEquals("Invalid or expired refresh token", exception.getMessage());
        verify(appUserRepository, never()).findByUsername(any());
        verify(appUserRepository, never()).save(any());
    }

    @Test
    void testRefreshToken_WhenUserNotFound_ShouldThrowException() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest(REFRESH_TOKEN);
        when(tokenProvider.validateRefreshToken(REFRESH_TOKEN)).thenReturn(TEST_USERNAME);
        when(appUserRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.empty());

        // When & Then
        InvalidRefreshTokenException exception = assertThrows(
                InvalidRefreshTokenException.class,
                () -> userService.refreshToken(request)
        );
        
        assertEquals("Invalid or expired refresh token", exception.getMessage());
        verify(appUserRepository, never()).save(any());
    }

    @Test
    void testRefreshToken_WhenTokenDoesNotMatch_ShouldThrowException() {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest(REFRESH_TOKEN);
        testUser.setRefreshToken("different.refresh.token");
        
        when(tokenProvider.validateRefreshToken(REFRESH_TOKEN)).thenReturn(TEST_USERNAME);
        when(appUserRepository.findByUsername(TEST_USERNAME)).thenReturn(Optional.of(testUser));

        // When & Then
        InvalidRefreshTokenException exception = assertThrows(
                InvalidRefreshTokenException.class,
                () -> userService.refreshToken(request)
        );
        
        assertEquals("Invalid or expired refresh token", exception.getMessage());
        verify(appUserRepository, never()).save(any());
    }

    @Test
    void testRegister_WithDifferentRoles_ShouldConvertCorrectly() {
        // Test ROLE_ADMIN
        RegisterUserRequestDto adminRequest = new RegisterUserRequestDto();
        adminRequest.setUsername("admin");
        adminRequest.setPassword("password");
        adminRequest.setEmail("admin@example.com");
        adminRequest.setPhoneNumber("1234567890");
        adminRequest.setRole(UserRole.ROLE_ADMIN);
        
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(appUserRepository.save(any(AppUserDocument.class))).thenReturn(testUser);

        userService.register(adminRequest);

        ArgumentCaptor<AppUserDocument> userCaptor = ArgumentCaptor.forClass(AppUserDocument.class);
        verify(appUserRepository).save(userCaptor.capture());
        assertTrue(userCaptor.getValue().getRoles().contains(Role.ROLE_ADMIN));
    }

    @Test
    void testRegister_WithCourierRole_ShouldConvertCorrectly() {
        // Given
        RegisterUserRequestDto courierRequest = new RegisterUserRequestDto();
        courierRequest.setUsername("courier");
        courierRequest.setPassword("password");
        courierRequest.setEmail("courier@example.com");
        courierRequest.setPhoneNumber("1234567890");
        courierRequest.setRole(UserRole.ROLE_COURIER);
        
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(appUserRepository.save(any(AppUserDocument.class))).thenReturn(testUser);

        // When
        userService.register(courierRequest);

        // Then
        ArgumentCaptor<AppUserDocument> userCaptor = ArgumentCaptor.forClass(AppUserDocument.class);
        verify(appUserRepository).save(userCaptor.capture());
        assertTrue(userCaptor.getValue().getRoles().contains(Role.ROLE_COURIER));
    }

    @Test
    void testRegister_WithRestaurantRole_ShouldConvertCorrectly() {
        // Given
        RegisterUserRequestDto restaurantRequest = new RegisterUserRequestDto();
        restaurantRequest.setUsername("restaurant");
        restaurantRequest.setPassword("password");
        restaurantRequest.setEmail("restaurant@example.com");
        restaurantRequest.setPhoneNumber("1234567890");
        restaurantRequest.setRole(UserRole.ROLE_RESTAURANT);
        
        when(passwordEncoder.encode(any())).thenReturn("encoded");
        when(appUserRepository.save(any(AppUserDocument.class))).thenReturn(testUser);

        // When
        userService.register(restaurantRequest);

        // Then
        ArgumentCaptor<AppUserDocument> userCaptor = ArgumentCaptor.forClass(AppUserDocument.class);
        verify(appUserRepository).save(userCaptor.capture());
        assertTrue(userCaptor.getValue().getRoles().contains(Role.ROLE_RESTAURANT));
    }
}
