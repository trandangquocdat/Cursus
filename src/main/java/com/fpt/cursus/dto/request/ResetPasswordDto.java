package com.fpt.cursus.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ResetPasswordDto {
    @NotBlank(message = "PASSWORD_NULL")
    private String password;
    @NotBlank(message = "PASSWORD_NULL")
    private String confirmPassword;
}
