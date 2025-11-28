package com.innowise.authservice.service;

import com.innowise.authservice.dto.*;

/**
 * Service interface for handling authentication-related operations.
 * Provides methods for user signup, login, token refresh, and token validation.
 */
public interface AuthService {
    /**
     * Registers a new user in the system.
     *
     * @param request the signup request containing user details
     * @return the created user response with user information
     */
    CredentialsResponse saveCredentials(SaveCredentialsRequest request);

    /**
     * Authenticates a user and generates access and refresh tokens.
     *
     * @param request the login request containing user credentials
     * @return the authentication response with access and refresh tokens
     */
    AuthResponse login(LoginRequest request);

    /**
     * Generates new access and refresh tokens using a valid refresh token.
     *
     * @param request the refresh token request containing the refresh token
     * @return the authentication response with new access and refresh tokens
     */
    AuthResponse refresh(RefreshTokenRequest request);

    /**
     * Validates if a given token is valid.
     *
     * @param request the validate token request containing the token to validate
     * @return true if the token is valid, false otherwise
     */
    Boolean validate(ValidateTokenRequest request);
}