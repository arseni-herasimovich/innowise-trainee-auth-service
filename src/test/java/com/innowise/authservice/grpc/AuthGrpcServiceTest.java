package com.innowise.authservice.grpc;

import com.innowise.authservice.generated.Auth;
import com.innowise.authservice.service.AuthService;
import io.grpc.stub.StreamObserver;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthGrpcServiceTest {
    @Mock
    private AuthService authService;

    @Mock
    private StreamObserver<Auth.DeleteUserResponse> responseObserver;

    @InjectMocks
    private AuthGrpcService authGrpcService;

    @Test
    @DisplayName("Should delete user")
    void givenUserId_whenDeleteUser_thenReturnsSuccess() {
        // Given
        var userId = UUID.randomUUID();

        var request = Auth.DeleteUserRequest.newBuilder()
                .setId(userId.toString())
                .build();

        // When
        when(authService.delete(userId)).thenReturn(true);

        authGrpcService.deleteUser(request, responseObserver);

        // Then
        verify(authService, times(1)).delete(userId);
        verify(responseObserver, times(1)).onNext(
                Auth.DeleteUserResponse.newBuilder()
                        .setSuccess(true)
                        .build()
        );
        verify(responseObserver, times(1)).onCompleted();
    }

    @Test
    @DisplayName("Should return error when exception occurred")
    void givenException_whenDeleteUser_thenReturnsStatusUnknown() {
        // Given
        var userId = UUID.randomUUID();

        var request = Auth.DeleteUserRequest.newBuilder()
                .setId(userId.toString())
                .build();

        // When
        when(authService.delete(userId)).thenThrow(RuntimeException.class);

        authGrpcService.deleteUser(request, responseObserver);

        // Then
        verify(authService, times(1)).delete(userId);
        verify(responseObserver, times(1)).onError(any());
        verify(responseObserver, never()).onNext(any());
        verify(responseObserver, never()).onCompleted();
    }
}
