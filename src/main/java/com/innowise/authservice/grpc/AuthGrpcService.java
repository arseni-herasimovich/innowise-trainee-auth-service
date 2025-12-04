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
        log.debug("Received delete user request with id: {}", request.getId());
        try {
            var success = authService.delete(UUID.fromString(request.getId()));
            var response = Auth.DeleteUserResponse.newBuilder()
                    .setSuccess(success)
                    .build();

            log.debug("User with id {} deleted successfully", request.getId());
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Error deleting user with id: {}", request.getId(), e);
            var status = Status.UNKNOWN.withDescription("Internal server error").asRuntimeException();
            responseObserver.onError(status);
        }
    }
}
