package com.fpt.cursus.exception.exceptions;

import lombok.Getter;

@Getter
public class AuthException extends RuntimeException {

    private Integer code;

    public AuthException(String message, Integer code) {
        super(message);
        this.code = code;
    }

}
