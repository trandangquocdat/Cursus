package com.fpt.cursus.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordDto {
    @NotBlank(message = "PASSWORD_NULL")
    @Pattern(regexp = "\\S+", message = "PASSWORD_CONTAINS_WHITESPACE")
    String currentPassword;
    @NotBlank(message = "PASSWORD_NULL")
    @Pattern(regexp = "\\S+", message = "PASSWORD_CONTAINS_WHITESPACE")
    @Size(min = 6, max = 18, message = "PASSWORD_SIZE_INVALID")
    String newPassword;
    @NotBlank(message = "PASSWORD_NULL")
    @Pattern(regexp = "\\S+", message = "PASSWORD_CONTAINS_WHITESPACE")
    @Size(min = 6, max = 18, message = "PASSWORD_SIZE_INVALID")
    String confirmNewPassword;
}
