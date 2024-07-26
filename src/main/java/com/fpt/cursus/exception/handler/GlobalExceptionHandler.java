package com.fpt.cursus.exception.handler;

import com.fpt.cursus.dto.response.ApiRes;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.util.ApiResUtil;
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.io.IOException;

@ControllerAdvice
public class GlobalExceptionHandler {
    private final ApiResUtil apiResUtil;

    @Autowired
    public GlobalExceptionHandler(ApiResUtil apiResUtil) {
        this.apiResUtil = apiResUtil;
    }

    //    @ExceptionHandler(Exception.class)
//    public ResponseEntity<?> handleRuntimeException(Exception exception) {
//        ApiRes<?> apiRes = apiResUtil.returnApiRes(false, ErrorCode.UNCATEGORIZED_ERROR.getCode(), ErrorCode.UNCATEGORIZED_ERROR.getMessage(), null);
//        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiRes);
//    }
    @ExceptionHandler(AppException.class)
    public ResponseEntity<?> handleAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        ApiRes<?> apiRes = apiResUtil.returnApiRes(false, errorCode.getCode(), errorCode.getMessage(), null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiRes);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrityViolationException(DataIntegrityViolationException exception) {
        String exceptionMessage = exception.getMessage();
        ApiRes<?> apiRes = apiResUtil.returnApiRes(false, HttpStatus.BAD_REQUEST.value(), exceptionMessage, null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiRes);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException exception) {
        String enumKey = exception.getFieldError().getDefaultMessage();
        ErrorCode errorCode = ErrorCode.valueOf(enumKey);
        String message = errorCode.getMessage();
        ApiRes<?> apiRes = apiResUtil.returnApiRes(false, errorCode.getCode(), message, null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiRes);
    }

    @ExceptionHandler(value = FirebaseAuthException.class)
    public ResponseEntity<ApiRes<?>> handleFirebaseAuthException(FirebaseAuthException exception) {
        String message = exception.getMessage();
        ApiRes<?> apiRes = apiResUtil.returnApiRes(false, HttpStatus.BAD_REQUEST.value(), message, null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .contentType(MediaType.APPLICATION_JSON)
                .body(apiRes);
    }

    @ExceptionHandler(value = IOException.class)
    public ResponseEntity<?> handleFirebaseAuthException(IOException exception) {
        String message = exception.getMessage();
        ApiRes<?> apiRes = apiResUtil.returnApiRes(false, HttpStatus.BAD_REQUEST.value(), message, null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiRes);
    }


}
