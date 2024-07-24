package com.fpt.cursus.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.cursus.dto.response.LoginResDto;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.service.AccountService;
import com.fpt.cursus.util.AccountUtil;
import com.fpt.cursus.util.TokenHandler;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@WebMvcTest(TokenController.class)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        TokenHandler.class,
        AccountUtil.class,
        AccountService.class
})
class TokenControllerTest {

    @MockBean
    private TokenHandler tokenHandler;

    @MockBean
    private AccountUtil accountUtil;

    @MockBean
    private AccountService accountService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new TokenController(tokenHandler, accountUtil, accountService))
                .alwaysDo(print())
                .alwaysExpect(status().isOk())
                .build();
    }

    @Test
    void testGetRefreshToken() throws Exception {
        //given
        Account account = new Account();
        //when
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(tokenHandler.generateRefreshToken(any(Account.class))).thenReturn("mockedRefreshToken");
        //then
        mockMvc.perform(get("/token/generate-refresh-token"))
                .andExpect(content().string("mockedRefreshToken"));
    }

    @Test
    void testRefreshToken() throws Exception {
        //given
        MockHttpServletRequest request = new MockHttpServletRequest();
        LoginResDto loginResDto = new LoginResDto();
        loginResDto.setAccessToken("mockedAccessToken");
        loginResDto.setRefreshToken("mockedRefreshToken");
        loginResDto.setExpire(1000L);
        //when
        when(accountService.refreshToken(any(HttpServletRequest.class))).thenReturn(loginResDto);
        //then
        mockMvc.perform(post("/token/refresh-token")
                        .requestAttr("request", request))
                .andExpect(content().json(objectMapper.writeValueAsString(loginResDto)));
    }
}
