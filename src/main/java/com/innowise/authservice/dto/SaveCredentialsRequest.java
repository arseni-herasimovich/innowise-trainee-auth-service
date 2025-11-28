package com.innowise.authservice.dto;

import jakarta.validation.constraints.*;

import java.util.UUID;

public record SaveCredentialsRequest(
        @NotNull(message = "ID is required")
        UUID id,

        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        @Size(max = 255)
        String email,

        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).{8,255}$",
                message = "Password must contain at least one lowercase letter, one uppercase letter, and one digit, and be between 8 and 255 characters long")
        @NotBlank(message = "Password is required")
        String password
) {
}
