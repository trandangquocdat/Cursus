package com.fpt.cursus.exception;

import com.fpt.cursus.dto.ApiRes;
import com.fpt.cursus.exception.exceptions.AuthException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class AuthExceptionHandler {
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ApiRes> duplicate(AuthException exception) {
        ApiRes apiRes = new ApiRes();
        apiRes.setStatus(false);
        apiRes.setMessage(exception.getMessage());
        apiRes.setCode(HttpStatus.UNAUTHORIZED.value());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiRes);
    }

    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<ApiRes> handleException(InternalAuthenticationServiceException exception) {
        ApiRes apiRes = new ApiRes();
        apiRes.setStatus(false);
        apiRes.setMessage(exception.getMessage());
        apiRes.setCode(HttpStatus.UNAUTHORIZED.value());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiRes);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiRes> handleException(AccessDeniedException exception) {
        ApiRes apiRes = new ApiRes();
        apiRes.setStatus(false);
        apiRes.setMessage("Access denied");
        apiRes.setCode(HttpStatus.UNAUTHORIZED.value());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiRes);
    }

}
