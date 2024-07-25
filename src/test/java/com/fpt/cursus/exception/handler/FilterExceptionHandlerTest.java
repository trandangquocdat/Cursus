package com.fpt.cursus.exception.handler;

import com.fpt.cursus.exception.exceptions.AuthException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.servlet.HandlerExceptionResolver;

import java.io.IOException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilterExceptionHandlerTest {

    @Mock
    private HandlerExceptionResolver resolver;

    @InjectMocks
    private FilterExceptionHandler filterExceptionHandler;

    @BeforeEach
    void setUp() {
    }

    @Test
    void commence() throws ServletException, IOException {

        //Given
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        AuthenticationException authenticationException = mock(AuthenticationException.class);

        //When
        filterExceptionHandler.commence(request,response,authenticationException);

        //Then
        verify(resolver).resolveException(eq(request), eq(response), isNull(), argThat(arg ->
                arg instanceof AuthException && ((AuthException) arg).getCode() == 403 && "Forbidden".equals(arg.getMessage())
        ));
    }
}