package com.fpt.cursus.dto;

import com.fpt.cursus.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.UniqueElements;

@Data
public class RegisterReqDto {
    @NotEmpty(message = "Please input username")
    @NotBlank (message = "Please don't leave BLANK in username")
    private String username;

    @NotEmpty(message = "Please input password")
    @NotBlank (message = "Please don't leave BLANK in password")
    private String password;

    @NotEmpty(message = "Please input email")
    @NotBlank(message = "Please don't leave BLANK in email")
    @Email(message = "Please input valid email")
    private String email;

    @NotEmpty (message = "Please input fullname")
    @Size(max = 200, message = "Fullname must be less than 200 characters")
    private String fullName;

    @NotEmpty(message = "Please input phone")
    @NotBlank(message = "Please don't leave BLANK in phone")

    private String phone;

    private Role role;
}
