package com.fpt.cursus.dto.request;

import com.fpt.cursus.enums.Gender;
import com.fpt.cursus.validator.NotEmptyOrNullMultipartFile;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
public class RegisterReqDto {
    @Size(min = 4, max = 18, message = "USERNAME_SIZE_INVALID")
    @NotBlank(message = "USERNAME_NULL")
    @Pattern(regexp = "\\S+", message = "USERNAME_CONTAINS_WHITESPACE")
    private String username;
    @Pattern(regexp = "\\S+", message = "PASSWORD_CONTAINS_WHITESPACE")
    @Size(min = 6, max = 18, message = "PASSWORD_SIZE_INVALID")
    @NotBlank(message = "PASSWORD_NULL")
    private String password;
    @NotBlank(message = "EMAIL_NULL")
    @Email(message = "EMAIL_INVALID")
    private String email;
    @Size(max = 200, message = "FULLNAME_INVALID_SIZE")
    @NotBlank(message = "FULLNAME_NULL")
    private String fullName;
    @NotBlank(message = "PHONE_NULL")
    private String phone;
    @NotEmptyOrNullMultipartFile()
    private MultipartFile avatar;
    private Gender gender;

}
