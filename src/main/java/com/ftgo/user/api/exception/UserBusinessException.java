package com.ftgo.user.api.exception;

/**
 * @author AmirHossein ZamanZade
 * @since 4/18/25
 */
public class UserBusinessException extends Exception {

    public UserBusinessException(String message) {
        super(message);
    }

    public UserBusinessException(String message, Throwable cause) {
        super(message, cause);
    }

    public String getErrorCode() {
        return this.getClass().getSimpleName();
    }
}
