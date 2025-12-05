package com.innowise.authservice.grpc;

import com.innowise.authservice.generated.AuthServiceGrpc;
import com.innowise.authservice.generated.Auth;
import com.innowise.authservice.service.AuthService;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;

import java.util.UUID;

@GrpcService
@RequiredArgsConstructor
@Slf4j
public class AuthGrpcService extends AuthServiceGrpc.AuthServiceImplBase {
    private final AuthService authService;

    @Override
    public void deleteUser(Auth.DeleteUserRequest request, StreamObserver<Auth.DeleteUserResponse> responseObserver) {
        log.debug("Received delete user request with id: {}", request.getUserId());
        try {
            var success = authService.delete(UUID.fromString(request.getUserId()));
            var response = Auth.DeleteUserResponse.newBuilder()
                    .setSuccess(success)
                    .build();

            log.debug("User with id {} deleted successfully", request.getUserId());
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (IllegalArgumentException e) {
            log.debug("User ID format is incorrect. ID: {}", request.getUserId());
            var status = Status.INVALID_ARGUMENT.withDescription("Invalid user ID format").asRuntimeException();
            responseObserver.onError(status);
        } catch (Exception e) {
            log.error("Error deleting user with id: {}", request.getUserId(), e);
            var status = Status.UNKNOWN.withDescription("Internal server error").asRuntimeException();
            responseObserver.onError(status);
        }
    }
}
