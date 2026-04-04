package com.frigapp.frigappapi.controller;

import com.frigapp.frigappapi.controller.api.AuthApi;
import com.frigapp.frigappapi.model.*;
import com.frigapp.frigappapi.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthService authService;
    private final HttpServletRequest httpServletRequest;

    @Override
    public ResponseEntity<AuthRegisterPost201Response> authRegisterPost(RegisterRequest registerRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(registerRequest));
    }

    @Override
    public ResponseEntity<AuthLoginPost200Response> authLoginPost(LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @Override
    public ResponseEntity<AuthLoginPost200Response> authRefreshPost() {
        String authHeader = httpServletRequest.getHeader("Authorization");
        return ResponseEntity.ok(authService.refresh(authHeader));
    }
}