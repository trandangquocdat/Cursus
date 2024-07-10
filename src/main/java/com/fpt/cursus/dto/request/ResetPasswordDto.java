package com.fpt.cursus.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordDto {
    @NotBlank(message = "PASSWORD_NULL")
    @Pattern(regexp = "\\S+", message = "PASSWORD_CONTAINS_WHITESPACE")
    private String password;
    @NotBlank(message = "PASSWORD_NULL")
    @Pattern(regexp = "\\S+", message = "PASSWORD_CONTAINS_WHITESPACE")
    private String confirmPassword;
}
