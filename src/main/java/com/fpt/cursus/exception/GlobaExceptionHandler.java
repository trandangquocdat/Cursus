package com.fpt.cursus.exception;

import com.fpt.cursus.dto.ApiRes;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

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
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public ResponseEntity<ApiRes> handleValidation(MethodArgumentNotValidException exception) {
        ApiRes apiRes = new ApiRes();
        apiRes.setStatus(false);

        FieldError fieldError = exception.getBindingResult().getFieldError();
        if (fieldError != null) {
            apiRes.setMessage(translateFieldError(fieldError));
        } else {
            apiRes.setMessage("Lỗi xác thực không xác định");
        }

        apiRes.setCode(HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(apiRes);
    }

    private String translateFieldError(FieldError fieldError) {
        String fieldName = fieldError.getField();
        String defaultMessage = fieldError.getDefaultMessage();
        return String.format("Trường '%s' %s", fieldName, translateErrorMessage(defaultMessage));
    }

    private String translateErrorMessage(String defaultMessage) {
        switch (defaultMessage) {
            case "must not be empty":
                return "không được để trống";
            case "must not be blank":
                return "không được chứa khoảng trắng";
            case "must not be null":
                return "không được là null";
            case "must be a well-formed email address":
                return "phải là địa chỉ email hợp lệ";
            // Add more cases as needed
            default:
                return defaultMessage;  // Default to the original message if no translation is found
        }
    }
}
