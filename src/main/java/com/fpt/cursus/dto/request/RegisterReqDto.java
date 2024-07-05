package com.fpt.cursus.dto.request;

import com.fpt.cursus.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterReqDto {
    @Size(min = 4, max = 18, message = "USERNAME_SIZE_INVALID")
    @NotBlank(message = "USERNAME_NULL")
    private String username;
    @Size(min = 6, max = 18, message = "PASSWORD_SIZE_INVALID")
    @NotBlank(message = "PASSWORD_NULL")
    private String password;
    @Email(message = "EMAIL_INVALID")
    private String email;
    @Size(max = 200, message = "FULLNAME_INVALID_SIZE")
    private String fullName;
    @NotBlank(message = "PHONE_NULL")
    private String phone;
    private String avatar;
    private Gender gender;

}
