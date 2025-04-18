package com.ftgo.user.api.exception;

import com.tosan.validation.util.ValidationViolationInfo;

import java.util.List;
import java.util.Map;

/**
 * @author AmirHossein ZamanZade
 * @since 4/18/25
 */
public class ValidationException extends UserServiceRuntimeException {

    private Map<String, List<ValidationViolationInfo>> validationViolationInfo;

    public ValidationException(String message) {
        super(message);
    }

    public ValidationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ValidationException(String message, Map<String, List<ValidationViolationInfo>> validationViolationInfo) {
        super(message);
        this.validationViolationInfo = validationViolationInfo;
    }

    public ValidationException(String message, Throwable cause,
            Map<String, List<ValidationViolationInfo>> validationViolationInfo) {
        super(message, cause);
        this.validationViolationInfo = validationViolationInfo;
    }
}
