package com.ftgo.user.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ftgo.user.advice.ExceptionParamDto;
import com.ftgo.user.api.exception.UserBusinessException;
import com.ftgo.user.api.exception.UserServiceRuntimeException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author AmirHossein ZamanZade
 * @since 4/25/2023
 */
@Component
@RequiredArgsConstructor
public class ExceptionHandlerUtil {

    private final ObjectMapper objectMapper;

    public ExceptionParamDto generateExceptionParam(Throwable exception) {
        Map<String, Object> exceptionParams;
        if (exception instanceof UserBusinessException e) {
            exceptionParams = convertToExceptionParam(exception);
            return generateExceptionParam(e.getMessage(), e.getErrorCode(), exceptionParams);
        } else if (exception instanceof UserServiceRuntimeException e) {
            exceptionParams = convertToExceptionParam(exception);
            return generateExceptionParam(e.getMessage(), e.getErrorCode(), exceptionParams);
        }
        return handleThrowable(exception);
    }

    private <T> Map<String, Object> convertToExceptionParam(T object) {
        return jsonToMap(objectToJsonForRestException(object));
    }

    private Map<String, Object> jsonToMap(String jsonString) {
        try {
            TypeReference<Map<String, Object>> typeRef = new TypeReference<>() {
            };
            return objectMapper.readValue(jsonString, typeRef);
        } catch (Exception e) {
            throw new UserServiceRuntimeException("error in converting Json to map JSONObject", e);
        }
    }

    private <T> String objectToJsonForRestException(T object) {
        try {
            return objectMapper.writer().writeValueAsString(object);
        } catch (Exception e) {
            throw new UserServiceRuntimeException("error in converting object to Json", e);
        }
    }

    private ExceptionParamDto handleThrowable(Throwable throwable) {
        UserServiceRuntimeException serviceException = new UserServiceRuntimeException(throwable.getMessage());
        return generateExceptionParam(serviceException.getMessage(), serviceException.getErrorCode(),
                null);
    }

    private ExceptionParamDto generateExceptionParam(String message, String errorCode,
            Map<String, Object> errorParams) {
        ExceptionParamDto exceptionParam = new ExceptionParamDto();
        exceptionParam.setMessage(message);
        exceptionParam.setErrorCode(errorCode);
        exceptionParam.setErrorParams(errorParams);
        return exceptionParam;
    }
}
