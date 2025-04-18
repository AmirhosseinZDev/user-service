package com.ftgo.user.userservice.exception.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ExceptionParamDto {

    private String exceptionName;
    private String message;
    private String path;
    private int status;
    private LocalDateTime timestamp;

}
