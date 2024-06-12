package com.fpt.cursus.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ResetPasswordDto {
    @NotBlank(message = "PASSWORD_NULL")
    @Pattern(regexp = "^[^\\s]+$", message = "PASSWORD_CONTAINS_WHITESPACE")
    private String password;
    @NotBlank(message = "PASSWORD_NULL")
    @Pattern(regexp = "^[^\\s]+$", message = "PASSWORD_CONTAINS_WHITESPACE")
    private String confirmPassword;
}
