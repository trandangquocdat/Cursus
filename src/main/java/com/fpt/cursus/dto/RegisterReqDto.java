package com.fpt.cursus.dto;

import com.fpt.cursus.enums.Role;
import lombok.Data;

@Data
public class RegisterReqDto {
    private String username;
    private String password;
    private String email;
    private String avatar;
    private String fullName;
    private String phone;
    private Role role;
}
