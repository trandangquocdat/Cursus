package com.fpt.cursus.exception.handler;

import com.fpt.cursus.dto.response.ApiRes;
import com.fpt.cursus.exception.exceptions.AuthException;
import com.fpt.cursus.util.ApiResUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class AuthExceptionHandler {
    @Autowired
    private ApiResUtil apiResUtil;
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<?> duplicate(AuthException exception) {
        ApiRes<?> apiRes = apiResUtil.returnApiRes(false,HttpStatus.UNAUTHORIZED.value(), exception.getMessage(),null );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiRes);
    }

    @ExceptionHandler(InternalAuthenticationServiceException.class)
    public ResponseEntity<?> handleException(InternalAuthenticationServiceException exception) {
        ApiRes<?> apiRes = apiResUtil.returnApiRes(false,HttpStatus.UNAUTHORIZED.value(), exception.getMessage(),null );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiRes);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<?> handleException(AccessDeniedException exception) {
        ApiRes<?> apiRes = apiResUtil.returnApiRes(false,HttpStatus.FORBIDDEN.value(), "Forbidden",null );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(apiRes);
    }

}
