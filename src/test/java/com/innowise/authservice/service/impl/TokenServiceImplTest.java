package com.innowise.authservice.service.impl;

import com.innowise.authservice.entity.User;
import com.innowise.authservice.repository.RefreshTokenRepository;
import com.innowise.authservice.security.JwtTokenProvider;
import org.apache.commons.codec.digest.HmacUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class TokenServiceImplTest {
    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private HmacUtils hmacUtils;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    @Spy
    private TokenServiceImpl tokenService;

    @Test
    @DisplayName("Should generate Auth Response for user")
    void givenUser_whenGenerateAuthResponse_thenReturnsAuthResponse() {
        // Given
        var user = new User();
        user.setId(UUID.randomUUID());
        user.setRole("ROLE_USER");

        // When
        when(jwtTokenProvider.generateAccessToken(eq(user.getId()), any())).thenReturn("ACCESS");
        when(jwtTokenProvider.generateRefreshToken(user.getId())).thenReturn("REFRESH");

        var response = tokenService.generateAuthResponse(user);

        // Then
        assertEquals("ACCESS", response.accessToken());
        assertEquals("REFRESH", response.refreshToken());

        verify(jwtTokenProvider, times(1)).generateAccessToken(eq(user.getId()), any());
        verify(jwtTokenProvider, times(1)).generateRefreshToken(user.getId());
        verify(refreshTokenRepository, times(1)).save(any());
        verify(tokenService, times(1)).hashToken("REFRESH");
    }

    @Test
    @DisplayName("Should hash token")
    void givenToken_whenHashToken_thenReturnsHashedToken() {
        // Given
        var token = "TOKEN";

        // When
        when(hmacUtils.hmacHex(token)).thenReturn("HASHED_TOKEN");

        var hashedToken = tokenService.hashToken(token);

        //Then
        assertEquals("HASHED_TOKEN", hashedToken);
        verify(hmacUtils, times(1)).hmacHex(token);
    }

    @Test
    @DisplayName("Should validate token")
    void givenToken_whenValid_thenReturnsTrue() {
        // Given
        var token = "TOKEN";

        // When
        when(jwtTokenProvider.validateToken(token)).thenReturn(true);

        var isValid = tokenService.validate(token);

        //Then
        assertTrue(isValid);
        verify(jwtTokenProvider, times(1)).validateToken(token);
    }
}
