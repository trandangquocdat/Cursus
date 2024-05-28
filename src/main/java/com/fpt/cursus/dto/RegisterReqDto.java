package com.fpt.cursus.dto;

import com.fpt.cursus.enums.Role;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.UniqueElements;

@Data
public class RegisterReqDto {
    @NotEmpty
    private String username;
    @NotEmpty
    private String password;
    @NotEmpty
    private String email;
    @NotEmpty
    @Size(max = 200, message = "Vui lòng nhập dưới 200 kí tự")
    private String fullName;
    @NotEmpty
    private String phone;
    private Role role;
}
