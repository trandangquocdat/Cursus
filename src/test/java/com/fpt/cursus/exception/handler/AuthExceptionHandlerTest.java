package com.fpt.cursus.exception.handler;

import com.fpt.cursus.dto.response.ApiRes;
import com.fpt.cursus.exception.exceptions.AuthException;
import com.fpt.cursus.util.ApiResUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InternalAuthenticationServiceException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthExceptionHandlerTest {

    @Mock
    private ApiResUtil apiResUtil;

    @InjectMocks
    private AuthExceptionHandler authExceptionHandler;

    @BeforeEach
    void setUp() {
    }

    @Test
    void duplicateTest() {

        //Given
        AuthException mockException = mock(AuthException.class);
        var mockApiRes = new ApiRes<>();
        mockApiRes.setStatus(false);
        mockApiRes.setCode(HttpStatus.UNAUTHORIZED.value());
        mockApiRes.setMessage(mockException.getMessage());
        mockApiRes.setData(null);

        //When
        when(apiResUtil.returnApiRes(false, HttpStatus.UNAUTHORIZED.value(), mockException.getMessage(), null)).thenReturn(mockApiRes);
        ResponseEntity<?> responseEntity = authExceptionHandler.duplicate(mockException);

        //Then
        assertEquals(responseEntity.getStatusCode(), HttpStatus.UNAUTHORIZED);
        assertEquals(responseEntity.getBody(), mockApiRes);
    }

    @Test
    void handleExceptionTest() {

        //Given
        InternalAuthenticationServiceException mockException = mock(InternalAuthenticationServiceException.class);
        var mockApiRes = new ApiRes<>();
        mockApiRes.setStatus(false);
        mockApiRes.setCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
        mockApiRes.setMessage(mockException.getMessage());
        mockApiRes.setData(null);

        //When
        when(apiResUtil.returnApiRes(false, HttpStatus.INTERNAL_SERVER_ERROR.value(), mockException.getMessage(), null)).thenReturn(mockApiRes);
        ResponseEntity<?> responseEntity = authExceptionHandler.handleException(mockException);

        //Then
        assertEquals(responseEntity.getStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR);
        assertEquals(responseEntity.getBody(), mockApiRes);
    }

    @Test
    void handleExceptionAccessTest() {

        //Given
        AccessDeniedException mockException = mock(AccessDeniedException.class);
        var mockApiRes = new ApiRes<>();
        mockApiRes.setStatus(false);
        mockApiRes.setCode(HttpStatus.FORBIDDEN.value());
        mockApiRes.setMessage("Forbidden");
        mockApiRes.setData(null);

        //When
        when(apiResUtil.returnApiRes(false, HttpStatus.FORBIDDEN.value(), "Forbidden", null)).thenReturn(mockApiRes);
        ResponseEntity<?> responseEntity = authExceptionHandler.handleException(mockException);

        //Then
        assertEquals(responseEntity.getStatusCode(), HttpStatus.FORBIDDEN);
        assertEquals(responseEntity.getBody(), mockApiRes);
    }
}