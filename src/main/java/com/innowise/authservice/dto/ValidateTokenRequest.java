package com.innowise.authservice.dto;

import jakarta.validation.constraints.NotBlank;

public record ValidateTokenRequest(
        @NotBlank(message = "Token is required")
        String token
) {
}
