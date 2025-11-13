package com.innowise.authservice.controller;

import com.innowise.authservice.dto.*;
import com.innowise.authservice.repository.RefreshTokenRepository;
import com.innowise.authservice.repository.UserRepository;
import com.innowise.authservice.service.AuthService;
import com.innowise.authservice.service.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class AuthControllerTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private AuthService authService;

    @BeforeEach
    void cleanRepositories() {
        userRepository.deleteAll();
        refreshTokenRepository.deleteAll();
    }

    @Container
    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("eureka.client.enabled", () -> false);
        registry.add("spring.security.jwt.secret", () ->
                "3552afa25b4ebf0860695d78172f7fa79a0b2fc58bba7b41bfbcf8b63630d06c");
    }

    @Nested
    @DisplayName("Signup")
    class SignupTests {
        @Test
        @DisplayName("Should sign up when user does not exist")
        void givenNotExistingUser_whenSignup_thenReturnsUserResponse() {
            // Given
            var request = new SignupRequest(
                    UUID.randomUUID(),
                    "NEW@EMAIL",
                    "Password1"
            );

            // When
            var response = restTemplate.exchange(
                    URI.SIGNUP,
                    HttpMethod.POST,
                    new HttpEntity<>(request),
                    new ParameterizedTypeReference<ApiResponse<UserResponse>>() {
                    }
            );

            // Then
            assertEquals(HttpStatus.CREATED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isSuccess());
            assertNotNull(response.getBody().getData());

            assertEquals(request.id(), response.getBody().getData().id());
            assertEquals(request.email(), response.getBody().getData().email());
        }

        @Test
        @DisplayName("Should return CONFLICT when user email exists")
        void givenExistingUserEmail_whenSignup_thenReturnsConflict() {
            // Given
            var signupRequest = registerUser();

            var request = new SignupRequest(
                    UUID.randomUUID(),
                    signupRequest.email(),
                    "Password1"
            );

            // When
            var response = restTemplate.exchange(
                    URI.SIGNUP,
                    HttpMethod.POST,
                    new HttpEntity<>(request),
                    new ParameterizedTypeReference<ApiResponse<UserResponse>>() {
                    }
            );

            // Then
            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
            assertNotNull(response.getBody());
            assertFalse(response.getBody().isSuccess());
            assertNull(response.getBody().getData());
        }

        @Test
        @DisplayName("Should return CONFLICT when user id exists")
        void givenExistingUserId_whenSignup_thenReturnsConflict() {
            // Given
            var signupRequest = registerUser();

            var request = new SignupRequest(
                    signupRequest.id(),
                    "TEST@EMAIL",
                    "Password1"
            );

            // When
            var response = restTemplate.exchange(
                    URI.SIGNUP,
                    HttpMethod.POST,
                    new HttpEntity<>(request),
                    new ParameterizedTypeReference<ApiResponse<UserResponse>>() {
                    }
            );

            // Then
            assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
            assertNotNull(response.getBody());
            assertFalse(response.getBody().isSuccess());
            assertNull(response.getBody().getData());
        }

        @Test
        @DisplayName("Should return BAD REQUEST when request is invalid")
        void givenInvalidRequest_whenSignup_thenReturnsBadRequest() {
            // Given
            var request = Map.of(
                    "id", UUID.randomUUID(),
                    "email", "TEST",
                    "password", "1"
            );

            // When
            var response = restTemplate.exchange(
                    URI.SIGNUP,
                    HttpMethod.POST,
                    new HttpEntity<>(request),
                    new ParameterizedTypeReference<ApiResponse<Void>>() {
                    }
            );

            // Then
            assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
            assertNotNull(response.getBody());
            assertFalse(response.getBody().isSuccess());
            assertNull(response.getBody().getData());
        }
    }

    @Nested
    @DisplayName("Login")
    class LoginTests {
        @Test
        @DisplayName("Should login when user exists and credentials correct")
        void givenValidUser_whenLogin_thenReturnsAuthResponse() {
            // Given
            var signupRequest = registerUser();

            var request = new LoginRequest(
                    signupRequest.email(),
                    "Password1"
            );

            // When
            var response = restTemplate.exchange(
                    URI.LOGIN,
                    HttpMethod.POST,
                    new HttpEntity<>(request),
                    new ParameterizedTypeReference<ApiResponse<AuthResponse>>() {
                    }
            );

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isSuccess());
            assertNotNull(response.getBody().getData());
            assertNotNull(response.getBody().getData().accessToken());
            assertNotNull(response.getBody().getData().refreshToken());
            assertNotEquals(response.getBody().getData().accessToken(), response.getBody().getData().refreshToken());

            assertTrue(tokenService.validate(response.getBody().getData().accessToken()));
            assertTrue(tokenService.validate(response.getBody().getData().refreshToken()));

        }

        @Test
        @DisplayName("Should return UNAUTHORIZED when user credentials incorrect")
        void givenInvalidUserCredentials_whenLogin_thenReturnsUnauthorized() {
            // Given
            var request = new LoginRequest(
                    "TEST@EMAIL",
                    "Password1"
            );

            // When
            var response = restTemplate.exchange(
                    URI.LOGIN,
                    HttpMethod.POST,
                    new HttpEntity<>(request),
                    new ParameterizedTypeReference<ApiResponse<AuthResponse>>() {
                    }
            );

            // Then
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertFalse(response.getBody().isSuccess());
            assertNull(response.getBody().getData());
        }
    }

    @Nested
    @DisplayName("Refresh token")
    class RefreshTokenTests {
        @Test
        @DisplayName("Should refresh token when refresh token exists")
        void givenValidRefreshToken_whenRefresh_thenReturnsAuthResponse() {
            // Given
            var signupRequest = registerUser();
            var authResponse = authService.login(new LoginRequest(signupRequest.email(), signupRequest.password()));
            var request = new RefreshTokenRequest(authResponse.refreshToken());

            // When
            var response = restTemplate.exchange(
                    URI.REFRESH,
                    HttpMethod.POST,
                    new HttpEntity<>(request),
                    new ParameterizedTypeReference<ApiResponse<AuthResponse>>() {
                    }
            );

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isSuccess());
            assertNotNull(response.getBody().getData());
            assertNotNull(response.getBody().getData().accessToken());
            assertNotNull(response.getBody().getData().refreshToken());
            assertNotEquals(response.getBody().getData().accessToken(), response.getBody().getData().refreshToken());

            assertTrue(tokenService.validate(response.getBody().getData().accessToken()));
            assertTrue(tokenService.validate(response.getBody().getData().refreshToken()));
        }

        @Test
        @DisplayName("Should return UNAUTHORIZED when refresh token does not exist")
        void givenInvalidRefreshToken_whenRefresh_thenReturnsUnauthorized() {
            // Given
            var request = new RefreshTokenRequest("INVALID_TOKEN");

            // When
            var response = restTemplate.exchange(
                    URI.REFRESH,
                    HttpMethod.POST,
                    new HttpEntity<>(request),
                    new ParameterizedTypeReference<ApiResponse<AuthResponse>>() {
                    }
            );

            // Then
            assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
            assertNotNull(response.getBody());
            assertFalse(response.getBody().isSuccess());
            assertNull(response.getBody().getData());
        }
    }

    @Nested
    @DisplayName("Validate token")
    class ValidateTokenTests {
        @Test
        @DisplayName("Should validate token when token exists")
        void givenAccessToken_whenValidate_thenReturnsTrue() {
            // Given
            var signupRequest = registerUser();
            var authResponse = authService.login(new LoginRequest(signupRequest.email(), signupRequest.password()));
            var request = new ValidateTokenRequest(authResponse.accessToken());

            // When
            var response = restTemplate.exchange(
                    URI.VALIDATE,
                    HttpMethod.POST,
                    new HttpEntity<>(request),
                    new ParameterizedTypeReference<ApiResponse<Boolean>>() {
                    }
            );

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isSuccess());
            assertTrue(response.getBody().getData());
            assertTrue(tokenService.validate(authResponse.accessToken()));
        }

        @Test
        @DisplayName("Should validate token when token exists")
        void givenRefreshToken_whenValidate_thenReturnsFalse() {
            // Given
            var signupRequest = registerUser();
            var authResponse = authService.login(new LoginRequest(signupRequest.email(), signupRequest.password()));
            var request = new ValidateTokenRequest(authResponse.refreshToken());

            // When
            var response = restTemplate.exchange(
                    URI.VALIDATE,
                    HttpMethod.POST,
                    new HttpEntity<>(request),
                    new ParameterizedTypeReference<ApiResponse<Boolean>>() {
                    }
            );

            // Then
            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertNotNull(response.getBody());
            assertTrue(response.getBody().isSuccess());
            assertFalse(response.getBody().getData());
            assertTrue(tokenService.validate(authResponse.accessToken()));
        }
    }

    private SignupRequest registerUser() {
        SignupRequest request = new SignupRequest(
                UUID.randomUUID(),
                "TEST@EMAIL",
                "Password1"
        );
        authService.signup(request);
        return request;
    }

    private static class URI {
        private static final String SIGNUP = "/api/v1/auth/signup";
        private static final String LOGIN = "/api/v1/auth/login";
        private static final String REFRESH = "/api/v1/auth/refresh";
        private static final String VALIDATE = "/api/v1/auth/validate";
    }
}
