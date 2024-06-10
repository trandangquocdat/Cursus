package com.fpt.cursus.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ChangePasswordDto {
    @NotEmpty(message = "Please input current password")
    @NotBlank(message = "Please don't leave BLANK in current password")
    String currentPassword;
    @NotEmpty(message = "Please input new password")
    @NotBlank (message = "Please don't leave BLANK in new password")
    String newPassword;
    @NotEmpty(message = "Please input confirm new password")
    @NotBlank (message = "Please don't leave BLANK in confirm new password")
    String confirmNewPassword;
}
