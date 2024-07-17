//package com.fpt.cursus.controller;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fpt.cursus.dto.request.*;
//import com.fpt.cursus.dto.response.LoginResDto;
//import com.fpt.cursus.entity.Account;
//import com.fpt.cursus.enums.Gender;
//import com.fpt.cursus.enums.Role;
//import com.fpt.cursus.service.AccountService;
//import com.fpt.cursus.service.OtpService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.mock.web.MockMultipartFile;
//import org.springframework.test.context.ContextConfiguration;
//import org.springframework.test.context.junit.jupiter.SpringExtension;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.Date;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.*;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
//import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
//
//@ExtendWith(SpringExtension.class)
//@WebMvcTest(AccountController.class)
//@ContextConfiguration(classes = {
//        AccountService.class,
//        OtpService.class
//})
//class AccountControllerTest {
//
//    @MockBean
//    private AccountService accountService;
//    @MockBean
//    private OtpService otpService;
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    private String username;
//    private String password;
//    private String email;
//    private String fullName;
//    private String phone;
//    private MockMultipartFile avatar;
//    private Gender gender;
//
//    @BeforeEach
//    void setUp() {
//        mockMvc = standaloneSetup(new AccountController(accountService, otpService))
//                .alwaysDo(print())
//                .build();
//
//        username = "username";
//        password = "password";
//        email = "test@test.com";
//        fullName = "fullName";
//        phone = "0123456789";
//        avatar = new MockMultipartFile("avatar",
//                "avatar.jpg",
//                "image/jpeg",
//                "avatar".getBytes());
//        gender = Gender.MALE;
//    }
//
//    @Test
//    void registerSuccess() throws Exception {
//        //given
//        Account newAccount = Account.builder()
//                .id(1L)
//                .username(username)
//                .password(password)
//                .email(email)
//                .fullName(fullName)
//                .phone(phone)
//                .role(Role.STUDENT)
//                .avatar(avatar.getOriginalFilename())
//                .gender(gender)
//                .build();
//        String otp = "otp";
//        //when
//        when(accountService.register(any(RegisterReqDto.class)))
//                .thenReturn(newAccount);
//        when(otpService.generateOtp())
//                .thenReturn(otp);
//        //then
//        mockMvc.perform(multipart("/register")
//                        .file(avatar)
//                        .param("username", username)
//                        .param("password", password)
//                        .param("email", email)
//                        .param("fullName", fullName)
//                        .param("phone", phone)
//                        .param("gender", gender.name())
//                        .contentType(MediaType.MULTIPART_FORM_DATA))
//                .andExpectAll(status().isCreated(),
//                        content().json(objectMapper.writeValueAsString(newAccount)));
//        verify(otpService, times(1))
//                .updateOldOtps(email);
//        verify(otpService, times(1))
//                .sendOtpEmail(email, otp);
//        verify(otpService, times(1))
//                .saveOtp(email, otp);
//    }
//
//    @Test
//    void loginSuccess() throws Exception {
//        //given
//        LoginReqDto loginReqDto = new LoginReqDto();
//        loginReqDto.setUsername(username);
//        loginReqDto.setPassword(password);
//
//        LoginResDto loginResDto = new LoginResDto();
//        loginResDto.setAccessToken("accessToken");
//        loginResDto.setRefreshToken("refreshToken");
//        loginResDto.setExpire(new Date().getTime());
//        //when
//        when(accountService.login(any(LoginReqDto.class)))
//                .thenReturn(loginResDto);
//        //then
//        mockMvc.perform(post("/login")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(loginReqDto)))
//                .andExpectAll(status().isOk(),
//                        content().json(objectMapper.writeValueAsString(loginResDto)));
//    }
//
//    @Test
//    void loginGoogleSuccess() throws Exception {
//        //given
//        LoginGoogleReq loginGoogleReq = new LoginGoogleReq();
//        loginGoogleReq.setToken("token");
//
//        LoginResDto loginResDto = new LoginResDto();
//        loginResDto.setAccessToken("accessToken");
//        loginResDto.setRefreshToken("refreshToken");
//        loginResDto.setExpire(new Date().getTime());
//        //when
//        when(accountService.loginGoogle(anyString()))
//                .thenReturn(loginResDto);
//        //then
//        mockMvc.perform(post("/login-google-firebase")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(loginGoogleReq)))
//                .andExpectAll(status().isOk(),
//                        content().json(objectMapper.writeValueAsString(loginResDto)));
//    }
//
//    @Test
//    void authenticateAccountSuccess() throws Exception {
//        //given
//        Account account = Account.builder()
//                .id(1L)
//                .username(username)
//                .password(password)
//                .email(email)
//                .fullName(fullName)
//                .phone(phone)
//                .role(Role.STUDENT)
//                .avatar(avatar.getOriginalFilename())
//                .gender(gender)
//                .build();
//        //when
//        when(accountService.authenticateAccount(anyString(), anyString()))
//                .thenReturn(account);
//        //then
//        mockMvc.perform(get("/auth/authenticate-account")
//                        .param("email", email)
//                        .param("otp", "otp"))
//                .andExpectAll(status().isOk(),
//                        content().json(objectMapper.writeValueAsString(account)));
//    }
//
//    @Test
//    void regenerateOtpSuccess() throws Exception {
//        //given
//        String message = "otp";
//        //when
//        when(accountService.regenerateOtp(anyString()))
//                .thenReturn("otp");
//        //then
//        mockMvc.perform(put("/auth/regenerate-otp")
//                        .param("email", email))
//                .andExpectAll(status().isOk(),
//                        content().string(message));
//    }
//
//    @Test
//    void changePasswordSuccess() throws Exception {
//        //given
//        ChangePasswordDto changePasswordDto = new ChangePasswordDto();
//        changePasswordDto.setCurrentPassword("currentPassword");
//        changePasswordDto.setNewPassword("newPassword");
//        changePasswordDto.setConfirmNewPassword("newPassword");
//        //then
//        mockMvc.perform(patch("/change-password")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(changePasswordDto)))
//                .andExpectAll(status().isOk(),
//                        content().string("Change password successfully"));
//    }
//
//    @Test
//    void forgotPasswordSuccess() throws Exception {
//        //then
//        mockMvc.perform(get("/auth/forgot-password")
//                        .param("email", email))
//                .andExpectAll(status().isOk(),
//                        content().string("Check your email to reset password"));
//    }
//
//    @Test
//    void resetPasswordSuccess() throws Exception {
//        //given
//        ResetPasswordDto resetPasswordDto = new ResetPasswordDto();
//        resetPasswordDto.setPassword("password");
//        resetPasswordDto.setConfirmPassword("password");
//        String message = "Reset password successfully";
//        //then
//        mockMvc.perform(put("/auth/reset-password")
//                        .param("email", email)
//                        .param("otp", "otp")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(resetPasswordDto)))
//                .andExpectAll(status().isOk(),
//                        content().string(message));
//    }
//}
