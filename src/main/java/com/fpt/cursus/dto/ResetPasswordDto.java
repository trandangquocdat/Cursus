package com.fpt.cursus.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ResetPasswordDto {
    @NotEmpty(message = "Please input password")
    @NotBlank(message = "Please don't leave BLANK in password")
    private String password;
    @NotEmpty(message = "Please input confirm new password")
    @NotBlank (message = "Please don't leave BLANK in confirm new password")
    private String confirmPassword;
}
