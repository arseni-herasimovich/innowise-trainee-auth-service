package com.innowise.authservice.exception;

import org.springframework.http.HttpStatus;

public class InvalidUserCredentialsException extends ApiException {
    public InvalidUserCredentialsException() {
        super("Invalid email or password", HttpStatus.UNAUTHORIZED);
    }
}
