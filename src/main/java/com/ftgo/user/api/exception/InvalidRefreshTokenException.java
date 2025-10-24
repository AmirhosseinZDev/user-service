package com.ftgo.user.api.exception;

public class InvalidRefreshTokenException extends UserServiceRuntimeException {
    public InvalidRefreshTokenException(String message) {
        super(message);
    }
}
