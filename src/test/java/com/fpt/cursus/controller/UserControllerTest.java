package com.fpt.cursus.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.cursus.dto.request.RegisterReqDto;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.enums.Role;
import com.fpt.cursus.enums.status.UserStatus;
import com.fpt.cursus.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;

    private RegisterReqDto request;
    private Account response;

    @BeforeEach
    void initData() {
        request = RegisterReqDto.builder()
                .username("test1")
                .password("123456")
                .email("test1@gmail.com")
                .fullName("test1")
                .phone("0972340212")
                .role(Role.STUDENT)
                .build();

        response = Account.builder()
                .id(1L)
                .username("test1")
                .password("123456")
                .email("test1@gmail.com")
                .fullName("test1")
                .status(UserStatus.INACTIVE)
                .phone("0972340212")
                .role(Role.STUDENT)
                .build();
    }

    @Test
    //
    void register_validRequest_success() throws Exception {
        //GIVEN
        ObjectMapper mapper = new ObjectMapper();
        String content = mapper.writeValueAsString(request);
        Mockito.when(userService.register(Mockito.any())).thenReturn(response);
        //WHEN
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                //THEN
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(true))
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(200))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(
                        "Register successfully. Please check your email to verify your account."))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.id").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.username").value("test1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.fullName").value("test1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.email").value("test1@gmail.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.phone").value("0972340212"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.role").value("STUDENT"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.result.status").value("INACTIVE")
                );


    }
    @Test
    void register_emptyUsername_fail() throws Exception {
        request.setUsername(null);
        ObjectMapper mapper = new ObjectMapper();
        String content = mapper.writeValueAsString(request);
        Mockito.when(userService.register(Mockito.any())).thenReturn(response);
        //WHEN
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                //THEN
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(false))
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(612))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(
                        "Username can not be null")
                );

    }
    @Test
    void register_whiteSpaceUsername_fail() throws Exception {
        request.setUsername("dat tran dang");
        ObjectMapper mapper = new ObjectMapper();
        String content = mapper.writeValueAsString(request);
        Mockito.when(userService.register(Mockito.any())).thenReturn(response);
        //WHEN
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                //THEN
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(false))
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(613))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(
                        "Username contains whitespace")
                );

    }

    @Test
    void register_invalidUsernameLength_fail() throws Exception {
        request.setUsername("d");
        ObjectMapper mapper = new ObjectMapper();
        String content = mapper.writeValueAsString(request);
        Mockito.when(userService.register(Mockito.any())).thenReturn(response);
        //WHEN
        mockMvc.perform(MockMvcRequestBuilders
                        .post("/register")
                        .contentType(MediaType.APPLICATION_JSON_VALUE)
                        .content(content))
                //THEN
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(false))
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(611))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value(
                        "Username must be between 4 and 18 characters")
                );

    }
}
