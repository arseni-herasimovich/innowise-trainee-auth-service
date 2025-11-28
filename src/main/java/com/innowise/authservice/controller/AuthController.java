package com.innowise.authservice.controller;

import com.innowise.authservice.dto.*;
import com.innowise.authservice.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/credentials")
    public ResponseEntity<ApiResponse<CredentialsResponse>> saveCredentials(@Valid @RequestBody SaveCredentialsRequest request) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User successfully signed up", authService.saveCredentials(request)));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> createToken(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("User successfully logged in", authService.login(request))
        );
    }

    @PostMapping("/validate")
    public ResponseEntity<ApiResponse<Boolean>> validateToken(@Valid @RequestBody ValidateTokenRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Token validated", authService.validate(request))
        );
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Token successfully refreshed", authService.refresh(request))
        );
    }
}
