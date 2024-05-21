package com.fpt.cursus.exception;

import com.fpt.cursus.exception.exceptions.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class AuthenticationHandleException {
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<?> duplicate(AuthenticationException exception) {
        return new ResponseEntity<String>(exception.getMessage(), HttpStatus.UNAUTHORIZED);
    }
}
