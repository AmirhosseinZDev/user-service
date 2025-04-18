package com.ftgo.user.userservice.auth.controller;

import com.ftgo.user.userservice.auth.dto.LoginRequest;
import com.ftgo.user.userservice.auth.dto.TokenResponse;
import com.ftgo.user.userservice.auth.service.api.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping(
            path = "/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public TokenResponse authenticateUser(@RequestBody LoginRequest loginRequest) {
        return authenticationService.login(loginRequest);
    }
}
