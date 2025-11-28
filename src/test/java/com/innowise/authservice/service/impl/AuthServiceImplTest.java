package com.innowise.authservice.service.impl;

import com.innowise.authservice.dto.*;
import com.innowise.authservice.entity.RefreshToken;
import com.innowise.authservice.entity.User;
import com.innowise.authservice.exception.InvalidRefreshTokenException;
import com.innowise.authservice.exception.InvalidUserCredentialsException;
import com.innowise.authservice.exception.UserAlreadyExistsException;
import com.innowise.authservice.mapper.UserMapper;
import com.innowise.authservice.repository.RefreshTokenRepository;
import com.innowise.authservice.repository.UserRepository;
import com.innowise.authservice.service.TokenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {
    @Mock
    private UserMapper userMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private AuthServiceImpl authService;

    @Test
    @DisplayName("Should sign up when user does not exist")
    void givenNotExistingUser_whenSignup_thenSavesUser() {
        // Given
        var request = new SaveCredentialsRequest(
                UUID.randomUUID(),
                "TEST@EMAIL",
                "PASSWORD"
        );

        var user = new User();
        user.setId(request.id());
        user.setEmail(request.email());
        user.setRole("ROLE_USER");

        var userResponse = new CredentialsResponse(
                user.getId(),
                user.getEmail(),
                user.getRole(),
                Instant.now(),
                Instant.now()
        );

        // When
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsById(request.id())).thenReturn(false);
        when(userMapper.toUser(request)).thenReturn(user);
        when(passwordEncoder.encode(request.password())).thenReturn("HASHED_PASSWORD");
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toUserResponse(user)).thenReturn(userResponse);

        var response = authService.saveCredentials(request);

        // Then
        assertEquals("HASHED_PASSWORD", user.getPassword());
        assertEquals(request.id(), response.id());
        assertEquals(request.email(), response.email());
        assertNotEquals(request.password(), user.getPassword());
        assertEquals(user.getRole(), response.role());

        verify(userRepository, times(1)).existsByEmail(request.email());
        verify(userRepository, times(1)).existsById(request.id());
        verify(userMapper, times(1)).toUser(request);
        verify(passwordEncoder, times(1)).encode(request.password());
        verify(userRepository, times(1)).save(user);
        verify(userMapper, times(1)).toUserResponse(user);
    }

    @Test
    @DisplayName("Should throw an exception when user email exists")
    void givenExistingUserEmail_whenSignup_thenThrowsException() {
        // Given
        var request = new SaveCredentialsRequest(
                UUID.randomUUID(),
                "TEST@EMAIL",
                "PASSWORD"
        );

        // When
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThrows(UserAlreadyExistsException.class, () -> authService.saveCredentials(request));
        verify(userRepository, times(1)).existsByEmail(request.email());
        verify(userRepository, never()).existsById(any());
        verify(userMapper, never()).toUser(any());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).toUserResponse(any());
    }

    @Test
    @DisplayName("Should throw an exception when user id exists")
    void givenExistingUserId_whenSignup_thenThrowsException() {
        // Given
        var request = new SaveCredentialsRequest(
                UUID.randomUUID(),
                "TEST@EMAIL",
                "PASSWORD"
        );

        // When
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.existsById(request.id())).thenReturn(true);

        // Then
        assertThrows(UserAlreadyExistsException.class, () -> authService.saveCredentials(request));

        verify(userRepository, times(1)).existsByEmail(request.email());
        verify(userRepository, times(1)).existsById(request.id());
        verify(userMapper, never()).toUser(any());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
        verify(userMapper, never()).toUserResponse(any());
    }

    @Test
    @DisplayName("Should login when user exists and credentials correct")
    void givenValidUser_whenLogin_thenReturnsAuthResponse() {
        // Given
        var request = new LoginRequest(
                "TEST@EMAIL",
                "PASSWORD"
        );

        var user = new User();
        user.setPassword("HASHED_PASSWORD");

        // When
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(true);
        when(tokenService.generateAuthResponse(user)).thenReturn(new AuthResponse("ACCESS", "REFRESH"));

        var response = authService.login(request);

        // Then
        assertEquals("ACCESS", response.accessToken());
        assertEquals("REFRESH", response.refreshToken());

        verify(userRepository, times(1)).findByEmail(request.email());
        verify(passwordEncoder, times(1)).matches(request.password(), user.getPassword());
        verify(tokenService, times(1)).generateAuthResponse(user);
    }

    @Test
    @DisplayName("Should throw an exception when user does not exist")
    void givenNotExistingUser_whenLogin_thenThrowsException() {
        // Given
        var request = new LoginRequest(
                "TEST@EMAIL",
                "PASSWORD"
        );

        // When
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        // Then
        assertThrows(InvalidUserCredentialsException.class, () -> authService.login(request));

        verify(userRepository, times(1)).findByEmail(request.email());
        verify(passwordEncoder, never()).matches(any(), any());
        verify(tokenService, never()).generateAuthResponse(any());
    }

    @Test
    @DisplayName("Should throw an exception when user credentials incorrect")
    void givenInvalidUserCredentials_whenLogin_thenThrowsException() {
        // Given
        var request = new LoginRequest(
                "TEST@EMAIL",
                "PASSWORD"
        );

        var user = new User();
        user.setPassword("HASHED_PASSWORD");

        // When
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(request.password(), user.getPassword())).thenReturn(false);

        // Then
        assertThrows(InvalidUserCredentialsException.class, () -> authService.login(request));

        verify(userRepository, times(1)).findByEmail(request.email());
        verify(passwordEncoder, times(1)).matches(request.password(), user.getPassword());
        verify(tokenService, never()).generateAuthResponse(any());
    }

    @Test
    @DisplayName("Should refresh token when refresh token exists")
    void givenValidRefreshToken_whenRefresh_thenReturnsAuthResponse() {
        // Given
        var request = new RefreshTokenRequest("REFRESH_TOKEN");
        var user = new User();
        var refreshToken = new RefreshToken();
        refreshToken.setIsRevoked(false);
        refreshToken.setExpiresAt(Instant.now().plusSeconds(10000));
        refreshToken.setUser(user);

        // When
        when(tokenService.validate(request.refreshToken())).thenReturn(true);
        when(tokenService.hashToken(request.refreshToken())).thenReturn("HASHED_REFRESH_TOKEN");
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(refreshToken));
        when(tokenService.generateAuthResponse(user)).thenReturn(new AuthResponse("ACCESS", "REFRESH"));

        var response = authService.refresh(request);

        // Then
        assertEquals("ACCESS", response.accessToken());
        assertEquals("REFRESH", response.refreshToken());

        verify(tokenService, times(1)).validate(request.refreshToken());
        verify(tokenService, times(1)).hashToken(request.refreshToken());
        verify(refreshTokenRepository, times(1)).findByTokenHash(any());
        verify(tokenService, times(1)).generateAuthResponse(user);
    }

    @Test
    @DisplayName("Should throw an exception when refresh token does not exist")
    void givenInvalidRefreshToken_whenRefresh_thenThrowsException() {
        // Given
        var request = new RefreshTokenRequest("REFRESH_TOKEN");

        // When
        when(tokenService.validate(request.refreshToken())).thenReturn(false);

        // Then
        assertThrows(InvalidRefreshTokenException.class, () -> authService.refresh(request));

        verify(tokenService, times(1)).validate(request.refreshToken());
        verify(tokenService, never()).hashToken(any());
        verify(refreshTokenRepository, never()).findByTokenHash(any());
        verify(tokenService, never()).generateAuthResponse(any());
    }

    @Test
    @DisplayName("Should throw an exception when refresh token revoked")
    void givenRevokedRefreshToken_whenRefresh_thenThrowsException() {
        // Given
        var request = new RefreshTokenRequest("REFRESH_TOKEN");
        var refreshToken = new RefreshToken();
        refreshToken.setIsRevoked(true);

        // When
        when(tokenService.validate(request.refreshToken())).thenReturn(true);
        when(tokenService.hashToken(request.refreshToken())).thenReturn("HASHED_REFRESH_TOKEN");
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(refreshToken));

        // Then
        assertThrows(InvalidRefreshTokenException.class, () -> authService.refresh(request));

        verify(tokenService, times(1)).validate(request.refreshToken());
        verify(tokenService, times(1)).hashToken(request.refreshToken());
        verify(refreshTokenRepository, times(1)).findByTokenHash(any());
        verify(tokenService, never()).generateAuthResponse(any());
    }

    @Test
    @DisplayName("Should throw an exception when refresh token expired in database (not in payload)")
    void givenExpiredRefreshToken_whenRefresh_thenThrowsException() {
        // Given
        var request = new RefreshTokenRequest("REFRESH_TOKEN");
        var refreshToken = new RefreshToken();
        refreshToken.setIsRevoked(false);
        refreshToken.setExpiresAt(Instant.now().minusSeconds(10000));

        // When
        when(tokenService.validate(request.refreshToken())).thenReturn(true);
        when(tokenService.hashToken(request.refreshToken())).thenReturn("HASHED_REFRESH_TOKEN");
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.of(refreshToken));

        // Then
        assertThrows(InvalidRefreshTokenException.class, () -> authService.refresh(request));

        verify(tokenService, times(1)).validate(request.refreshToken());
        verify(tokenService, times(1)).hashToken(request.refreshToken());
        verify(refreshTokenRepository, times(1)).findByTokenHash(any());
        verify(tokenService, never()).generateAuthResponse(any());
    }

    @Test
    @DisplayName("Should validate access token")
    void givenAccessToken_whenValidate_thenReturnsTrue() {
        // Given
        var request = new ValidateTokenRequest("TOKEN");

        // When
        when(tokenService.validate(request.token())).thenReturn(true);
        when(tokenService.isAccessToken(request.token())).thenReturn(true);

        var response = authService.validate(request);

        // Then
        assertTrue(response);

        verify(tokenService, times(1)).validate(request.token());
    }

    @Test
    @DisplayName("Should validate access token")
    void givenRefreshToken_whenValidate_thenReturnsFalse() {
        // Given
        var request = new ValidateTokenRequest("TOKEN");

        // When
        when(tokenService.validate(request.token())).thenReturn(true);
        when(tokenService.isAccessToken(request.token())).thenReturn(false);

        var response = authService.validate(request);

        // Then
        assertFalse(response);

        verify(tokenService, times(1)).validate(request.token());
    }
}
