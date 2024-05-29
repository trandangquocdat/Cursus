package com.fpt.cursus.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ChangePasswordDto {
    @NotEmpty
    String oldPassword;
    @NotEmpty
    String newPassword;
    @NotEmpty
    String confirmNewPassword;
}
