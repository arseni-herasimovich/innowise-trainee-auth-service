package com.innowise.authservice.lifecycle;

import com.innowise.authservice.repository.RefreshTokenRepository;
import com.innowise.authservice.security.JwtProperties;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Component
@RequiredArgsConstructor
public class RefreshTokenCleaner {
    private final JwtProperties properties;
    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(cron = "${spring.security.jwt.refresh-token-cleaner-cron}")
    @Transactional
    public void cleanExpiredTokens() {
        refreshTokenRepository.deleteAllByExpiresAtBefore(
                Instant.now().minus(properties.getRefreshTokenTtl(), ChronoUnit.SECONDS));
    }
}
