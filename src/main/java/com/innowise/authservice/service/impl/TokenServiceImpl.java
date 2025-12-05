package com.innowise.authservice.service.impl;

import com.innowise.authservice.dto.AuthResponse;
import com.innowise.authservice.entity.RefreshToken;
import com.innowise.authservice.entity.User;
import com.innowise.authservice.repository.RefreshTokenRepository;
import com.innowise.authservice.security.JwtTokenProvider;
import com.innowise.authservice.service.TokenService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Implementation of TokenService that handles generation of Auth Responses, validation
 * and gives access to hashing tokens and checking their type (access/refresh)
 */
@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {
    private final JwtTokenProvider jwtTokenProvider;
    private final HmacUtils hmacUtils;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public AuthResponse generateAuthResponse(User user) {
        var access = jwtTokenProvider.generateAccessToken(user.getUserId(), Map.of("role", user.getRole()));
        var refresh = jwtTokenProvider.generateRefreshToken(user.getUserId());
        saveRefreshToken(refresh, user);
        return new AuthResponse(access, refresh);
    }

    @Override
    public String hashToken(String token) {
        return hmacUtils.hmacHex(token);
    }

    @Override
    public boolean validate(String token) {
        return jwtTokenProvider.validateToken(token);
    }

    @Override
    public boolean isAccessToken(String token) {
        var role = jwtTokenProvider.getStringClaim(token, "role");
        return role != null && !role.isBlank();
    }

    /**
     * Saves a refresh token to the database with its hash, expiration date, and user reference.
     *
     * @param token the refresh token to save
     * @param user the user associated with the token
     */
    private void saveRefreshToken(String token, User user) {
        refreshTokenRepository.save(
                RefreshToken.builder()
                        .tokenHash(hashToken(token))
                        .user(user)
                        .expiresAt(jwtTokenProvider.getExpiresAt(token))
                        .isRevoked(false)
                        .build()
        );
    }
}
