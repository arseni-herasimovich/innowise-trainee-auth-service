package com.innowise.authservice.dto;

import java.time.Instant;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String email,
        String role,
        Instant createdAt,
        Instant updatedAt
) {
}
