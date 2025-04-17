package com.ftgo.user.userservice.exception.handler;

import com.ftgo.user.userservice.auth.controller.AuthController;
import com.ftgo.user.userservice.exception.dto.ExceptionParamDto;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice(assignableTypes = AuthController.class)
public class RestExceptionHandler {

    /**
     * Handles AuthenticationException (and its subclasses like BadCredentialsException).
     * Returns a standardized error response with HTTP status 401 Unauthorized.
     *
     * @param ex The caught AuthenticationException.
     * @param request The current HttpServletRequest.
     * @return ResponseEntity containing the ExceptionParamDto and 401 status.
     */
    @ExceptionHandler(AuthenticationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ExceptionParamDto handleAuthenticationException(AuthenticationException ex, HttpServletRequest request) {
        String path = request.getRequestURI();

        log.warn("Authentication failure at path [{}]: {}", path, ex.getMessage());

        return new ExceptionParamDto(
                ex.getClass().getSimpleName(),
                ex.getMessage(),
                path,
                HttpStatus.UNAUTHORIZED.value(),
                LocalDateTime.now()
        );
    }


    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ExceptionParamDto handleGenericException(Exception ex, HttpServletRequest request) {
        String path = request.getRequestURI();
        log.error("Unhandled exception occurred at path [{}]:", path, ex);

        return new ExceptionParamDto(
                ex.getClass().getSimpleName(),
                "An unexpected internal error occurred.",
                path,
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                LocalDateTime.now()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ExceptionParamDto handleValidationExceptions(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String path = request.getRequestURI();
        String message = ex.getBindingResult().getAllErrors().stream()
               .map(DefaultMessageSourceResolvable::getDefaultMessage)
               .collect(Collectors.joining(", "));
        log.warn("Validation failure at path [{}]: {}", path, message);

        return new ExceptionParamDto(
                ex.getClass().getSimpleName(),
                message,
                path,
                HttpStatus.BAD_REQUEST.value(),
                LocalDateTime.now()
        );
    }

}
