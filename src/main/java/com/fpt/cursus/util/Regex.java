package com.fpt.cursus.util;

import org.springframework.stereotype.Component;

@Component
public class Regex {
    public boolean isPhoneValid(String phone) {
        return phone.matches("^(0|\\+84)(3[2-9]|5[689]|7[06-9]|8[1-689]|9[0-46-9])(\\d{7})$");
    }
}
