package com.fpt.cursus.dto;

import com.fpt.cursus.enums.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.UniqueElements;

@Data
public class RegisterReqDto {
    @NotEmpty(message = "Vui lòng nhập username")
    @NotBlank (message = "Vui lòng không điền khoảng trắng trong username")
    private String username;
    @NotEmpty(message = "Vui lòng nhập password")
    @NotBlank (message = "Vui lòng không điền khoảng trắng trong password")
    private String password;
    @NotEmpty(message = "Vui lòng nhập email")
    @NotBlank(message = "Vui lòng không điền khoảng trắng trong email")
    private String email;

    @NotEmpty (message = "Vui lòng nhập fullname")
    @Size(max = 200, message = "Vui lòng nhập dưới 200 kí tự")
    private String fullName;
    @NotEmpty
    @NotBlank(message = "Vui lòng không điền khoảng trắng trong số điện thoại ")
    private String phone;
    private Role role;
}
