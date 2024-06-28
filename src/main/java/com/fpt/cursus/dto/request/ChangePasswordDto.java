package com.fpt.cursus.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordDto {
    @NotBlank(message = "PASSWORD_NULL")
    String currentPassword;
    @NotBlank(message = "PASSWORD_NULL")
    @Size(min = 6, max = 18, message = "PASSWORD_SIZE_INVALID")
    String newPassword;
    @NotBlank(message = "PASSWORD_NULL")
    @Size(min = 6, max = 18, message = "PASSWORD_SIZE_INVALID")
    String confirmNewPassword;
}
