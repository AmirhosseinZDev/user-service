package com.ftgo.user.api.dto;

import com.ftgo.user.api.dto.enumaration.UserRole;
import com.tosan.validation.constraints.MobileNumber;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * @author Amirhossein Zamanzade
 * @since 4/19/25
 */
@Data
public class RegisterUserRequestDto {

    @NotEmpty
    private String username;

    @NotEmpty
    private String password;

    @Email
    private String email;

    @MobileNumber
    private String phoneNumber;

    @NotNull
    private UserRole role;
}