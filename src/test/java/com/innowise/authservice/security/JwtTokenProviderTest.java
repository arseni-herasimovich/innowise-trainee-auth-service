package com.innowise.authservice.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtProperties properties;
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        properties = new JwtProperties();
        properties.setSecret("34c0ef79a25ea6979814b3341c3529484fd8168b831f58fac94e8f04a8bc7ee8");
        properties.setAccessTokenTtl(60L);
        properties.setRefreshTokenTtl(120L);
        jwtTokenProvider = new JwtTokenProvider(properties);
    }

    @Test
    @DisplayName("Should generate and validate access token")
    void givenRandomUUID_whenGenerateAccessToken_thenReturnToken() {
        UUID id = UUID.randomUUID();
        String token = jwtTokenProvider.generateAccessToken(id, Map.of("role", "ROLE_USER"));

        assertTrue(jwtTokenProvider.validateToken(token));
        assertEquals(id.toString(), Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.getSecret())))
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject());
    }

    @Test
    @DisplayName("Should return false for expired token")
    void givenExpiredToken_whenValidateToken_thenReturnFalse() {
        String token = Jwts.builder()
                .expiration(Date.from(Instant.now().minusSeconds(10)))
                .signWith(Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.getSecret())))
                .compact();

        assertFalse(jwtTokenProvider.validateToken(token));
    }

    @Test
    @DisplayName("Should return false for malformed token")
    void givenMalformedToken_whenValidateToken_thenReturnFalse() {
        assertFalse(jwtTokenProvider.validateToken("fake-token"));
    }
}

