package com.fpt.cursus.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ChangePasswordDto {
    @NotBlank(message = "PASSWORD_NULL")
    String currentPassword;
    @NotBlank(message = "PASSWORD_NULL")
    String newPassword;
    @NotBlank(message = "PASSWORD_NULL")
    String confirmNewPassword;
}
