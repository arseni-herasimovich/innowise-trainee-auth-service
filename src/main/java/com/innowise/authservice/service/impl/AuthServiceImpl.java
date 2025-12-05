package com.innowise.authservice.service.impl;

import com.innowise.authservice.dto.*;
import com.innowise.authservice.exception.InvalidRefreshTokenException;
import com.innowise.authservice.exception.InvalidUserCredentialsException;
import com.innowise.authservice.exception.UserAlreadyExistsException;
import com.innowise.authservice.mapper.UserMapper;
import com.innowise.authservice.repository.RefreshTokenRepository;
import com.innowise.authservice.repository.UserRepository;
import com.innowise.authservice.service.AuthService;
import com.innowise.authservice.service.TokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

/**
 * Implementation of AuthService that handles authentication-related operations.
 * Provides methods for user signup, login, token refresh, and token validation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final TokenService tokenService;

    @Override
    public CredentialsResponse saveCredentials(SaveCredentialsRequest request) {
        log.debug("Signing up user with email: {}", request.email());
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException(request.email());
        }

        var user = userMapper.toUser(request);
        user.setPassword(passwordEncoder.encode(request.password()));

        var savedUser = userRepository.save(user);
        log.debug("User with email: {} signed up successfully", savedUser.getEmail());
        return userMapper.toUserResponse(savedUser);
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        log.debug("Logging in user with email: {}", request.email());
        var user = userRepository.findByEmail(request.email()).orElseThrow(InvalidUserCredentialsException::new);
        if (passwordEncoder.matches(request.password(), user.getPassword())) {
            log.debug("User with email: {} logged in successfully", request.email());
            return tokenService.generateAuthResponse(user);
        } else {
            throw new InvalidUserCredentialsException();
        }
    }

    @Override
    public AuthResponse refresh(RefreshTokenRequest request) {
        log.debug("Refreshing token");
        if (!tokenService.validate(request.refreshToken())) {
            log.debug("Refresh token is invalid.");
            throw new InvalidRefreshTokenException();
        }

        var refreshTokenHash = tokenService.hashToken(request.refreshToken());
        var refreshToken = refreshTokenRepository.findByTokenHash(refreshTokenHash)
                .filter(token -> !token.getIsRevoked() && token.getExpiresAt().isAfter(Instant.now()))
                .orElseThrow(InvalidRefreshTokenException::new);

        log.debug("Refresh token is valid. Generating auth response");
        return tokenService.generateAuthResponse(refreshToken.getUser());
    }

    @Override
    public Boolean validate(ValidateTokenRequest request) {
        log.debug("Validating token");
        return tokenService.validate(request.token()) && tokenService.isAccessToken(request.token());
    }

    @Override
    public Boolean delete(UUID userId) {
        return userRepository.findByUserId(userId)
                .map(user -> {
                    userRepository.delete(user);
                    return true;
                })
                .orElse(false);
    }
}
