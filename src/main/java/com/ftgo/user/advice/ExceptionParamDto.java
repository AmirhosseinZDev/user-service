package com.ftgo.user.advice;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionParamDto {

    private String errorCode;
    private String message;
    private Map<String, Object> errorParams;
}
