package com.fpt.cursus.exception.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;


public enum ErrorCode {
    UNCATEGORIZED_ERROR(500, "Uncategorized error"),
    INTERNAL_SERVER_ERROR(500, "Internal server error"),
    BAD_REQUEST(400, "Bad request"),
    UNAUTHORIZED(401, "Unauthorized"),
    FORBIDDEN(403, "Forbidden"),
    USER_NOT_FOUND(404, "User not found"),
    USER_EXISTS(409, "User already exists"),
    PASSWORD_NOT_CORRECT(400,"Password is incorrect"),
    EMAIL_EXISTS(409, "Email already exists"),
    EMAIL_INVALID(400, "Email invalid"),
    EMAIL_UNAUTHENTICATED(401, "Email unauthenticated"),
    EMAIL_CAN_NOT_SEND(500, "Email can not send"),;


    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
    private int code;
    private String message;
    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
