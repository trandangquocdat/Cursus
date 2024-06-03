package com.fpt.cursus.exception;

import com.fpt.cursus.dto.ApiRes;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.ArrayList;
import java.util.List;

@ControllerAdvice
public class GlobaExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiRes> handleRuntimeException(Exception exception) {
        ApiRes apiRes = new ApiRes();
        apiRes.setStatus(false);
        apiRes.setMessage(ErrorCode.UNCATEGORIZED_ERROR.getMessage());
        apiRes.setCode(ErrorCode.UNCATEGORIZED_ERROR.getCode());
        return ResponseEntity.status(ErrorCode.UNCATEGORIZED_ERROR.getCode()).body(apiRes);
    }
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiRes> handleAppException(AppException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        ApiRes apiRes = new ApiRes();
        apiRes.setStatus(false);
        apiRes.setMessage(errorCode.getMessage());
        apiRes.setCode(errorCode.getCode());
        return ResponseEntity.status(exception.getErrorCode().getCode()).body(apiRes);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiRes> handleDataIntegrityViolationException(DataIntegrityViolationException exception) {
        ApiRes apiRes = new ApiRes();
        apiRes.setStatus(false);
        apiRes.setCode(HttpStatus.BAD_REQUEST.value());

        String exceptionMessage = exception.getMessage();
        String field = null;
        String detailMessage = null;

        if (exceptionMessage != null) {
            int detailStart = exceptionMessage.indexOf("Detail:") + 8;
            int detailEnd = exceptionMessage.indexOf("]", detailStart);
            if (detailStart > 0) {
                detailMessage = exceptionMessage.substring(detailStart, detailEnd);
            }
        }

        apiRes.setMessage( detailMessage);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiRes);
    }

    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ApiRes> handleValidation(MethodArgumentNotValidException exception) {
        ApiRes apiRes = new ApiRes();
        apiRes.setStatus(false);

        FieldError fieldError = exception.getBindingResult().getFieldError();
        if (fieldError != null) {
            apiRes.setMessage(infoFieldError(fieldError));
        } else {
            apiRes.setMessage("Not defined error");
        }

        apiRes.setCode(HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiRes);
    }

    private String infoFieldError(FieldError fieldError) {
        String fieldName = fieldError.getField();
        String defaultMessage = fieldError.getDefaultMessage();
        return String.format("Field '%s' %s", fieldName, defaultMessage);
    }

}
