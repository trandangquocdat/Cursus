package com.fpt.cursus.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class LoginReqDto {
    @NotEmpty(message = "Please input username")
    @NotBlank(message = "Please don't leave BLANK in username")
    private String username;
    @NotEmpty(message = "Please input password")
    @NotBlank (message = "Please don't leave BLANK in password")
    private String password;
}
