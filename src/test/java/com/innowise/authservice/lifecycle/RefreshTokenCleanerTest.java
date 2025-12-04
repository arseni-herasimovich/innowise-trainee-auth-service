package com.innowise.authservice.lifecycle;

import com.innowise.authservice.repository.RefreshTokenRepository;
import com.innowise.authservice.security.JwtProperties;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RefreshTokenCleanerTest {
    @Mock
    private JwtProperties properties;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private RefreshTokenCleaner refreshTokenCleaner;

    @Test
    @DisplayName("Should clean expired tokens")
    void givenExpiredTokens_whenCleanExpiredTokens_thenClean() {
        // When
        refreshTokenCleaner.cleanExpiredTokens();

        // Then
        verify(properties, times(1)).getRefreshTokenTtl();
        verify(refreshTokenRepository, times(1)).deleteAllByExpiresAtBefore(any());
    }
}
