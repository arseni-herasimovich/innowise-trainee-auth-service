package com.innowise.authservice.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtTokenProvider {
    private final JwtProperties properties;

    public Instant getExpiresAt(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration()
                .toInstant();
    }

    public String getStringClaim(String token, String claim) {
        return Jwts.parser()
                .verifyWith(getSignKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .get(claim, String.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSignKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("Token is expired");
        } catch (UnsupportedJwtException e) {
            log.debug("Unsupported JWT Exception. Message: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.debug("Malformed JWT Exception. Message: {}", e.getMessage());
        } catch (SecurityException e) {
            log.debug("Security exception while validating token. Message: {}", e.getMessage());
        } catch (Exception e) {
            log.debug("Unknown exception while validating token. Message: {}", e.getMessage());
        }
        return false;
    }

    public String generateAccessToken(UUID id, Map<String, Object> claims) {
        Date expiration = Date.from(Instant.now().plusSeconds(properties.getAccessTokenTtl()));
        return generateToken(id, expiration, claims);
    }

    public String generateRefreshToken(UUID id) {
        Date expiration = Date.from(Instant.now().plusSeconds(properties.getRefreshTokenTtl()));
        return generateToken(id, expiration, Map.of());
    }

    private String generateToken(UUID id, Date expiration, Map<String, Object> claims) {
        return Jwts
                .builder()
                .expiration(expiration)
                .subject(id.toString())
                .claims(claims)
                .signWith(getSignKey())
                .compact();
    }

    private SecretKey getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(properties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
