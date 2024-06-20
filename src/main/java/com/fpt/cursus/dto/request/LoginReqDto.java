package com.fpt.cursus.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class LoginReqDto {
    @NotBlank(message = "USERNAME_NULL")
    private String username;
    @NotBlank(message = "PASSWORD_NULL")
    private String password;
}
