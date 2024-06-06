package com.fpt.cursus.exception.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
public enum ErrorCode {

    //User error - 6xx
    UNCATEGORIZED_ERROR(666, "Uncategorized error"),
    USER_NOT_FOUND(600, "User not found"),
    USER_EXISTS(601, "User already exists"),

    //Password error - 7xx
    PASSWORD_NOT_CORRECT(700,"Password is incorrect"),
    PASSWORD_NOT_MATCH(701, "Password does not match"),
    PASSWORD_IS_SAME_CURRENT(702, "Password is same as current password"),
    //phone error - 75X -> 76X
    PHONE_NOT_VALID(750, "Phone not valid"),
    //email error - 80X -> 81X
    EMAIL_UNAUTHENTICATED(800, "Email unauthenticated"),
    EMAIL_NOT_FOUND(801, "Email not found"),
    EMAIL_INVALID(802, "Email invalid"),
    EMAIL_EXISTS(803, "Email already exists"),
    EMAIL_CAN_NOT_SEND(803, "Email can not send"),

    //OTP error - 85X
    OTP_INVALID(850,"Wrong OTP"),
    OTP_EXPIRED(851, "OTP expired"),
    ;



    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    private final int code;
    private final String message;

}
