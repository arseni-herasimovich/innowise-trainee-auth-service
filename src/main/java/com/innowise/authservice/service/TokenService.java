package com.innowise.authservice.service;

import com.innowise.authservice.dto.AuthResponse;
import com.innowise.authservice.entity.User;

/**
 * Service interface for handling generation of Auth Responses, validation,
 * hashing tokens and checking their type (access/refresh)
 */
public interface TokenService {
    /**
     * Generates authentication response with access and refresh tokens for a given user.
     *
     * @param user the user for whom to generate tokens
     * @return AuthResponse containing access and refresh tokens
     */
    AuthResponse generateAuthResponse(User user);

    /**
     * Hashes a token using HMAC algorithm for secure storage.
     *
     * @param token the token to hash
     * @return hashed representation of the token
     */
    String hashToken(String token);

    /**
     * Validates if a given token is valid and not expired.
     *
     * @param token the token to validate
     * @return true if the token is valid, false otherwise
     */
    boolean validate(String token);

    /**
     * Checks if a given token is an access token by verifying if it contains a role claim.
     *
     * @param token the token to check
     * @return true if the token is an access token, false otherwise
     */
    boolean isAccessToken(String token);
}
