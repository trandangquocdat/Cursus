package com.fpt.cursus.dto;

import com.fpt.cursus.enums.Role;
import lombok.Data;

@Data
public class LoginResDto {
    private String username;
    private String fullName;
    private String email;
    private String phone;
    private Role role;
    private String token;
}
