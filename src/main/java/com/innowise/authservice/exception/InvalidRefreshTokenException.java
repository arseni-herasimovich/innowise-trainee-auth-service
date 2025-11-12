package com.innowise.authservice.exception;

import org.springframework.http.HttpStatus;

public class InvalidRefreshTokenException extends ApiException{
    public InvalidRefreshTokenException() {
        super("Invalid refresh token!", HttpStatus.UNAUTHORIZED);
    }
}
