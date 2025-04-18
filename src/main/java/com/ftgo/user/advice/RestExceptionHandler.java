package com.ftgo.user.advice;

import com.ftgo.user.api.exception.UserBusinessException;
import com.ftgo.user.api.exception.UserServiceRuntimeException;
import com.ftgo.user.api.exception.ValidationException;
import com.ftgo.user.util.ExceptionHandlerUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

    private final ExceptionHandlerUtil exceptionHandlerUtil;

    /**
     * Handles AuthenticationException (and its subclasses like BadCredentialsException).
     * Returns a standardized error response with HTTP status 401 Unauthorized.
     *
     * @param ex The caught AuthenticationException.
     * @return ResponseEntity containing the ExceptionParamDto and 401 status.
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ExceptionParamDto handleAuthenticationException(AuthenticationException ex) {
        return exceptionHandlerUtil.generateExceptionParam(ex);
    }

    @ExceptionHandler(UserBusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionParamDto handleGenericException(UserBusinessException ex) {
        return exceptionHandlerUtil.generateExceptionParam(ex);
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionParamDto handleValidationExceptions(ValidationException ex) {
        return exceptionHandlerUtil.generateExceptionParam(ex);
    }

    @ExceptionHandler(UserServiceRuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionParamDto handleGenericException(UserServiceRuntimeException ex) {
        return exceptionHandlerUtil.generateExceptionParam(ex);
    }

    @ExceptionHandler(Throwable.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionParamDto handleThrowableException(Throwable ex) {
        return exceptionHandlerUtil.generateExceptionParam(ex);
    }
}
