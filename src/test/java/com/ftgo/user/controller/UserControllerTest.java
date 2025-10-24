package com.ftgo.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ftgo.user.api.dto.LoginRequest;
import com.ftgo.user.api.dto.RefreshTokenRequest;
import com.ftgo.user.api.dto.RegisterUserRequestDto;
import com.ftgo.user.api.dto.TokenResponse;
import com.ftgo.user.api.dto.enumaration.UserRole;
import com.ftgo.user.api.exception.InvalidRefreshTokenException;
import com.ftgo.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    private MockMvc mockMvc;

    private ObjectMapper objectMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    private static final String ACCESS_TOKEN = "access.token.jwt";
    private static final String REFRESH_TOKEN = "refresh.token.jwt";
    private static final String NEW_ACCESS_TOKEN = "new.access.token.jwt";
    private static final String NEW_REFRESH_TOKEN = "new.refresh.token.jwt";

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(userController).build();
        objectMapper = new ObjectMapper();
    }

    @Test
    void testLogin_WithValidCredentials_ShouldReturnTokens() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("testUser", "password");
        TokenResponse tokenResponse = new TokenResponse(ACCESS_TOKEN, REFRESH_TOKEN);
        when(userService.login(any(LoginRequest.class))).thenReturn(tokenResponse);

        // When & Then
        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value(ACCESS_TOKEN))
                .andExpect(jsonPath("$.refreshToken").value(REFRESH_TOKEN));

        verify(userService).login(any(LoginRequest.class));
    }

    @Test
    void testLogin_ShouldAcceptJsonRequest() throws Exception {
        // Given
        String jsonRequest = "{\"username\":\"testUser\",\"password\":\"password\"}";
        TokenResponse tokenResponse = new TokenResponse(ACCESS_TOKEN, REFRESH_TOKEN);
        when(userService.login(any(LoginRequest.class))).thenReturn(tokenResponse);

        // When & Then
        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk());
    }

    @Test
    void testRegister_WithValidData_ShouldSucceed() throws Exception {
        // Given
        RegisterUserRequestDto requestDto = new RegisterUserRequestDto();
        requestDto.setUsername("newUser");
        requestDto.setPassword("password123");
        requestDto.setEmail("newuser@example.com");
        requestDto.setPhoneNumber("1234567890");
        requestDto.setRole(UserRole.ROLE_USER);
        
        doNothing().when(userService).register(any(RegisterUserRequestDto.class));

        // When & Then
        mockMvc.perform(post("/user/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());

        verify(userService).register(any(RegisterUserRequestDto.class));
    }

    @Test
    void testRefreshToken_WithValidToken_ShouldReturnNewTokens() throws Exception {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest(REFRESH_TOKEN);
        TokenResponse tokenResponse = new TokenResponse(NEW_ACCESS_TOKEN, NEW_REFRESH_TOKEN);
        when(userService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(tokenResponse);

        // When & Then
        mockMvc.perform(post("/user/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.token").value(NEW_ACCESS_TOKEN))
                .andExpect(jsonPath("$.refreshToken").value(NEW_REFRESH_TOKEN));

        verify(userService).refreshToken(any(RefreshTokenRequest.class));
    }

    @Test
    void testRefreshToken_WithInvalidToken_ShouldThrowException() throws Exception {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest("invalid.token");
        when(userService.refreshToken(any(RefreshTokenRequest.class)))
                .thenThrow(new InvalidRefreshTokenException("Invalid or expired refresh token"));

        // When & Then - expecting exception to be thrown
        try {
            mockMvc.perform(post("/user/refresh-token")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().is5xxServerError());
        } catch (Exception e) {
            // Expected - the exception is wrapped in ServletException
            assertTrue(e.getCause() instanceof InvalidRefreshTokenException);
        }

        verify(userService).refreshToken(any(RefreshTokenRequest.class));
    }

    @Test
    void testRefreshToken_ShouldAcceptJsonRequest() throws Exception {
        // Given
        String jsonRequest = "{\"refreshToken\":\"" + REFRESH_TOKEN + "\"}";
        TokenResponse tokenResponse = new TokenResponse(NEW_ACCESS_TOKEN, NEW_REFRESH_TOKEN);
        when(userService.refreshToken(any(RefreshTokenRequest.class))).thenReturn(tokenResponse);

        // When & Then
        mockMvc.perform(post("/user/refresh-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.refreshToken").exists());
    }

    @Test
    void testLogin_ShouldRequireJsonContentType() throws Exception {
        // Given
        LoginRequest loginRequest = new LoginRequest("testUser", "password");

        // When & Then - should fail without proper content type
        mockMvc.perform(post("/user/login")
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void testRefreshToken_ShouldRequireJsonContentType() throws Exception {
        // Given
        RefreshTokenRequest request = new RefreshTokenRequest(REFRESH_TOKEN);

        // When & Then - should fail without proper content type
        mockMvc.perform(post("/user/refresh-token")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void testRegister_ShouldRequireJsonContentType() throws Exception {
        // Given
        RegisterUserRequestDto requestDto = new RegisterUserRequestDto();
        requestDto.setUsername("newUser");
        requestDto.setPassword("password123");
        requestDto.setEmail("newuser@example.com");
        requestDto.setPhoneNumber("1234567890");
        requestDto.setRole(UserRole.ROLE_USER);

        // When & Then - should fail without proper content type
        mockMvc.perform(post("/user/register")
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isUnsupportedMediaType());
    }
}
