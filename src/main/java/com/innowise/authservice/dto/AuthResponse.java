package com.innowise.authservice.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken
) {
}
