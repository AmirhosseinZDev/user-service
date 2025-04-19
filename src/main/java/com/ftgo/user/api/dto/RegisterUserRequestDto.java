package com.ftgo.user.api.dto;

import com.ftgo.user.api.dto.enumaration.UserRole;
import com.tosan.validation.constraints.Expression;
import com.tosan.validation.constraints.Expressions;
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
@Expressions(
        @Expression(
                identifier = "RegisterUserRequestDto.whenRoleEqualUserOrCourierThenPhoneNumberMustHaveValue",
                value = "((this.role eq ROLE_USER or this.role eq ROLE_COURIER) and this.phoneNumber ne null) or" +
                        "((this.role eq ROLE_RESTAURANT or this.role eq ROLE_ADMIN) and this.phoneNumber eq null) ",
                message = "if role equal to ROLE_USER or ROLE_COURIER then phone number must have value otherwise must be null.")
)
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
