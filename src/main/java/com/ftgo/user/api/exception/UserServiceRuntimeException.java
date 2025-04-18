package com.ftgo.user.api.exception;

/**
 * @author AmirHossein ZamanZade
 * @since 4/18/25
 */
public class UserServiceRuntimeException extends RuntimeException {

    public UserServiceRuntimeException(String message) {
        super(message);
    }

    public UserServiceRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getErrorCode() {
        return this.getClass().getSimpleName();
    }
}
