package com.fpt.cursus.controller;

import com.fpt.cursus.dto.response.LoginResDto;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.enums.Role;
import com.fpt.cursus.service.AccountService;
import com.fpt.cursus.util.AccountUtil;
import com.fpt.cursus.util.TokenHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TokenControllerTest {

    @Mock
    private TokenHandler tokenHandler;

    @Mock
    private AccountUtil accountUtil;

    @Mock
    private AccountService accountService;

    @InjectMocks
    private TokenController tokenController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetRefreshToken() {
        // Mocking getCurrentAccount method of AccountUtil
        Account mockAccount = new Account();
        mockAccount.setUsername("mockuser");
        mockAccount.setEmail("mock@example.com");
        mockAccount.setRole(Role.STUDENT);

        when(accountUtil.getCurrentAccount()).thenReturn(mockAccount);
        when(tokenHandler.generateRefreshToken(any(Account.class))).thenReturn("mockedRefreshToken");

        ResponseEntity<Object> response = tokenController.getRefreshToken();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("mockedRefreshToken", response.getBody()); // Check if the generated token matches
    }

    @Test
    void testRefreshToken() {
        // Mocking refreshToken method of AccountService
        LoginResDto mockLoginResDto = new LoginResDto();
        mockLoginResDto.setAccessToken("mockedAccessToken");
        mockLoginResDto.setRefreshToken("mockedRefreshToken");
        mockLoginResDto.setExpire(3600L);

        HttpServletRequest mockRequest = mock(HttpServletRequest.class);

        when(accountService.refreshToken(mockRequest)).thenReturn(mockLoginResDto);

        ResponseEntity<Object> response = tokenController.refreshToken(mockRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        LoginResDto responseDto = (LoginResDto) response.getBody();
        assertEquals(mockLoginResDto.getAccessToken(), responseDto.getAccessToken()); // Check if access token matches
        assertEquals(mockLoginResDto.getRefreshToken(), responseDto.getRefreshToken()); // Check if refresh token matches
        assertEquals(mockLoginResDto.getExpire(), responseDto.getExpire()); // Check if expiration matches
    }
}
