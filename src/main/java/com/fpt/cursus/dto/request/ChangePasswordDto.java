package com.fpt.cursus.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ChangePasswordDto {
    @NotBlank(message = "PASSWORD_NULL")
    @Pattern(regexp = "^[^\\s]+$", message = "PASSWORD_CONTAINS_WHITESPACE")
    String currentPassword;
    @NotBlank(message = "PASSWORD_NULL")
    @Pattern(regexp = "^[^\\s]+$", message = "PASSWORD_CONTAINS_WHITESPACE")
    String newPassword;
    @NotBlank(message = "PASSWORD_NULL")
    @Pattern(regexp = "^[^\\s]+$", message = "PASSWORD_CONTAINS_WHITESPACE")
    String confirmNewPassword;
}
