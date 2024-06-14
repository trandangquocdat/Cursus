package com.fpt.cursus.exception.handler;

import com.fpt.cursus.dto.response.ApiRes;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.util.ApiResUtil;
import com.google.firebase.auth.FirebaseAuthException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobaExceptionHandler {
    @Autowired
    private ApiResUtil apiResUtil;
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleRuntimeException(Exception exception) {
        ApiRes<?> apiRes = apiResUtil.returnApiRes(false, ErrorCode.UNCATEGORIZED_ERROR.getCode(), ErrorCode.UNCATEGORIZED_ERROR.getMessage(), null);
        return ResponseEntity.status(ErrorCode.UNCATEGORIZED_ERROR.getCode()).body(apiRes);
    }
    @ExceptionHandler(AppException.class)
    public ResponseEntity<?> handleAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        ApiRes<?> apiRes = apiResUtil.returnApiRes(false, errorCode.getCode(), errorCode.getMessage(), null);
        return ResponseEntity.status(exception.getErrorCode().getCode()).body(apiRes);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> handleDataIntegrityViolationException(DataIntegrityViolationException exception) {
        String exceptionMessage = exception.getMessage();
        String detailMessage = null;
        if (exceptionMessage != null) {
            int detailStart = exceptionMessage.indexOf("Detail:") + 8;
            int detailEnd = exceptionMessage.indexOf("]", detailStart);
            if (detailStart > 0) {
                detailMessage = exceptionMessage.substring(detailStart, detailEnd);
            }
        }
        ApiRes<?> apiRes = apiResUtil.returnApiRes(false, HttpStatus.BAD_REQUEST.value(), detailMessage, null);
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
    public ResponseEntity<?> handleFirebaseAuthException(FirebaseAuthException exception) {
        String message = exception.getMessage();
        ApiRes<?> apiRes = apiResUtil.returnApiRes(false, HttpStatus.BAD_REQUEST.value(), message, null);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiRes);
    }


}
