package com.fpt.cursus.exception.handler;

import com.fpt.cursus.dto.response.ApiRes;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.util.ApiResUtil;
import com.google.firebase.auth.FirebaseAuthException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GlobalExceptionHandlerTest {

    @Mock
    private ApiResUtil apiResUtil;

    @InjectMocks
    private GlobalExceptionHandler globaExceptionHandler;

    @BeforeEach
    void setUp() {
    }

    @Test
    void handleAppExceptionTest() {

        //Given
        AppException mockAppException = new AppException(ErrorCode.UNCATEGORIZED_ERROR);
        var mockApiRes = new ApiRes<>();
        mockApiRes.setStatus(false);
        mockApiRes.setCode(HttpStatus.BAD_REQUEST.value());
        mockApiRes.setMessage(mockAppException.getMessage());
        mockApiRes.setData(null);

        //When
        when(apiResUtil.returnApiRes(false, mockAppException.getErrorCode().getCode(), mockAppException.getMessage(), null)).thenReturn(mockApiRes);
        ResponseEntity<?> responseEntity = globaExceptionHandler.handleAppException(mockAppException);

        //Then
        assertEquals(responseEntity.getStatusCode(), HttpStatus.BAD_REQUEST);
        assertEquals(responseEntity.getBody(), mockApiRes);
    }

    @Test
    void handleDataIntegrityViolationExceptionTest() {

        //Given
        DataIntegrityViolationException mockException = new DataIntegrityViolationException("Error");
        var mockApiRes = new ApiRes<>();
        mockApiRes.setStatus(false);
        mockApiRes.setCode(HttpStatus.BAD_REQUEST.value());
        mockApiRes.setMessage(mockException.getMessage());
        mockApiRes.setData(null);

        //When
        when(apiResUtil.returnApiRes(false, HttpStatus.BAD_REQUEST.value(), mockException.getMessage(), null)).thenReturn(mockApiRes);
        ResponseEntity<?> responseEntity = globaExceptionHandler.handleDataIntegrityViolationException(mockException);

        //Then
        assertEquals(responseEntity.getStatusCode(), HttpStatus.BAD_REQUEST);
        assertEquals(responseEntity.getBody(), mockApiRes);
    }

    @Test
    void handleValidationTest() {

        //Given
        FieldError fieldError = mock(FieldError.class);
        MethodArgumentNotValidException mockException = mock(MethodArgumentNotValidException.class);
        String enumKey = ErrorCode.UNCATEGORIZED_ERROR.name();
        ErrorCode errorCode = ErrorCode.valueOf(enumKey);
        String message = errorCode.getMessage();
        var mockApiRes = new ApiRes<>();
        mockApiRes.setStatus(false);
        mockApiRes.setCode(errorCode.getCode());
        mockApiRes.setMessage(message);
        mockApiRes.setData(null);

        //When
        when(mockException.getFieldError()).thenReturn(fieldError);
        when(mockException.getFieldError().getDefaultMessage()).thenReturn(enumKey);
        when(apiResUtil.returnApiRes(false, errorCode.getCode(), message, null)).thenReturn(mockApiRes);
        ResponseEntity<?> responseEntity = globaExceptionHandler.handleValidation(mockException);

        //Then
        assertEquals(responseEntity.getStatusCode(), HttpStatus.BAD_REQUEST);
        assertEquals(responseEntity.getBody(), mockApiRes);
    }

    @Test
    void handleFirebaseAuthExceptionTest() {

        //Given
        FirebaseAuthException mockException = mock(FirebaseAuthException.class);
        var mockApiRes = new ApiRes<>();
        mockApiRes.setStatus(false);
        mockApiRes.setCode(HttpStatus.BAD_REQUEST.value());
        mockApiRes.setMessage(mockException.getMessage());
        mockApiRes.setData(null);

        //When
        when(apiResUtil.returnApiRes(false, HttpStatus.BAD_REQUEST.value(), mockException.getMessage(), null)).thenReturn(mockApiRes);
        ResponseEntity<?> responseEntity = globaExceptionHandler.handleFirebaseAuthException(mockException);

        //Then
        assertEquals(responseEntity.getStatusCode(), HttpStatus.BAD_REQUEST);
        assertEquals(responseEntity.getBody(), mockApiRes);
        assertEquals(responseEntity.getHeaders().getContentType(), MediaType.APPLICATION_JSON);
    }

    @Test
    void handleFirebaseAuthExceptionIOTest() {

        //Given
        IOException mockException = mock(IOException.class);
        var mockApiRes = new ApiRes<>();
        mockApiRes.setStatus(false);
        mockApiRes.setCode(HttpStatus.BAD_REQUEST.value());
        mockApiRes.setMessage(mockException.getMessage());
        mockApiRes.setData(null);

        //When
        when(apiResUtil.returnApiRes(false, HttpStatus.BAD_REQUEST.value(), mockException.getMessage(), null)).thenReturn(mockApiRes);
        ResponseEntity<?> responseEntity = globaExceptionHandler.handleFirebaseAuthException(mockException);

        //Then
        assertEquals(responseEntity.getStatusCode(), HttpStatus.BAD_REQUEST);
        assertEquals(responseEntity.getBody(), mockApiRes);
    }
}