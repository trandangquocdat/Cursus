package com.fpt.cursus.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.cursus.dto.request.*;
import com.fpt.cursus.dto.response.ApiRes;
import com.fpt.cursus.dto.response.LoginResDto;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.enums.status.UserStatus;
import com.fpt.cursus.enums.type.Gender;
import com.fpt.cursus.enums.type.Role;
import com.fpt.cursus.service.AccountService;
import com.fpt.cursus.service.OtpService;
import com.fpt.cursus.util.ApiResUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

@ExtendWith(SpringExtension.class)
@WebMvcTest(AccountController.class)
@ContextConfiguration(classes = {
        AccountService.class,
        OtpService.class,
        ApiResUtil.class
})
class AccountControllerTest {

    @MockBean
    private AccountService accountService;
    @MockBean
    private OtpService otpService;
    @MockBean
    private ApiResUtil apiResUtil;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mockMvc = standaloneSetup(new AccountController(accountService, otpService, apiResUtil))
                .alwaysDo(print())
                .build();
    }

    @Test
    void testRegister_Success() throws Exception {
        //given
        RegisterReqDto reqDto = RegisterReqDto.builder()
                .username("tester")
                .password("123456")
                .email("test@test.com")
                .fullName("tester")
                .phone("123456789")
                .gender(Gender.MALE)
                .avatar("avatar.png")
                .build();

        String json = objectMapper.writeValueAsString(reqDto);

        Account account = getAccount(reqDto);

        String otp = "otp";

        ApiRes<Object> apiRes = new ApiRes<>();
        apiRes.setCode(null);
        apiRes.setStatus(null);
        apiRes.setMessage(null);
        apiRes.setData(account);
        //when
        when(accountService.register(any(RegisterReqDto.class)))
                .thenReturn(account);
        when(otpService.generateOtp())
                .thenReturn(otp);
        when(apiResUtil.returnApiRes(any(), any(), any(), any()))
                .thenReturn(apiRes);
        //then
        mockMvc.perform(post("/auth/register")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpectAll(status().isOk(),
                        jsonPath("$.data.username").value(account.getUsername()),
                        jsonPath("$.data.email").value(account.getEmail()),
                        jsonPath("$.data.fullName").value(account.getFullName()),
                        jsonPath("$.data.phone").value(account.getPhone()),
                        jsonPath("$.data.gender").value(account.getGender().toString()),
                        jsonPath("$.data.role").value(account.getRole().toString()),
                        jsonPath("$.data.avatar").value(account.getAvatar()),
                        jsonPath("$.data.status").value(account.getStatus().toString()));
        verify(otpService, times(1))
                .updateOldOtps(anyString());
        verify(otpService, times(1))
                .sendOtpEmail(anyString(), anyString());
        verify(otpService, times(1))
                .saveOtp(anyString(), anyString());
    }

    private static Account getAccount(RegisterReqDto reqDto) {
        Account account = new Account();
        account.setUsername(reqDto.getUsername());
        account.setEmail(reqDto.getEmail());
        account.setFullName(reqDto.getFullName());
        account.setPhone(reqDto.getPhone());
        account.setGender(reqDto.getGender());
        account.setCreatedDate(new Date());
        account.setPassword(reqDto.getPassword());
        account.setRole(Role.STUDENT);
        account.setAvatar(reqDto.getAvatar());
        account.setStatus(UserStatus.INACTIVE);
        return account;
    }

    @Test
    void testLogin_Success() throws Exception {
        //given
        LoginReqDto reqDto = new LoginReqDto();
        reqDto.setUsername("tester");
        reqDto.setPassword("123456");

        String json = objectMapper.writeValueAsString(reqDto);

        LoginResDto resDto = new LoginResDto();
        resDto.setAccessToken("access_token");
        resDto.setRefreshToken("refresh_token");
        resDto.setExpire(5000);

        ApiRes<Object> apiRes = new ApiRes<>();
        apiRes.setCode(null);
        apiRes.setStatus(null);
        apiRes.setMessage(null);
        apiRes.setData(resDto);
        //when
        when(accountService.login(any(LoginReqDto.class)))
                .thenReturn(resDto);
        when(apiResUtil.returnApiRes(any(), any(), any(), any()))
                .thenReturn(apiRes);
        //then
        mockMvc.perform(post("/auth/login")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpectAll(status().isOk(),
                        jsonPath("$.data.accessToken").value(resDto.getAccessToken()),
                        jsonPath("$.data.refreshToken").value(resDto.getRefreshToken()),
                        jsonPath("$.data.expire").value(resDto.getExpire()));
    }

    @Test
    void testLoginGoogle_Success() throws Exception {
        //given
        LoginGoogleReq req = new LoginGoogleReq();
        req.setToken("token");
        String json = objectMapper.writeValueAsString(req);

        LoginResDto resDto = new LoginResDto();
        resDto.setAccessToken("access_token");
        resDto.setRefreshToken("refresh_token");
        resDto.setExpire(5000);

        ApiRes<Object> apiRes = new ApiRes<>();
        apiRes.setCode(null);
        apiRes.setStatus(null);
        apiRes.setMessage(null);
        apiRes.setData(resDto);
        //when
        when(accountService.loginGoogle(anyString()))
                .thenReturn(resDto);
        when(apiResUtil.returnApiRes(any(), any(), any(), any()))
                .thenReturn(apiRes);
        //then
        mockMvc.perform(post("/auth/login-google-firebase")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpectAll(status().isOk(),
                        jsonPath("$.data.accessToken").value(resDto.getAccessToken()),
                        jsonPath("$.data.refreshToken").value(resDto.getRefreshToken()),
                        jsonPath("$.data.expire").value(resDto.getExpire()));
    }


    @Test
    void testVerifyAccount_Success() throws Exception {
        //given
        String successMessage = "Verify account successfully. You can now login with your email and password.";
        ApiRes<Object> apiRes = new ApiRes<>();
        apiRes.setCode(null);
        apiRes.setStatus(null);
        apiRes.setMessage(successMessage);
        apiRes.setData(null);
        //when
        when(apiResUtil.returnApiRes(any(), any(), any(), any()))
                .thenReturn(apiRes);
        //then
        mockMvc.perform(get("/auth/verify-account")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("email", "test@test.com")
                        .param("otp", "otp"))
                .andExpectAll(status().isOk(),
                        jsonPath("$.message").value(successMessage));
    }

    @Test
    void testVerifyInstructor() throws Exception {
        //given
        CvLinkDto cvLinkDto = new CvLinkDto();
        cvLinkDto.setCvLink("https://www.google.com");
        String json = objectMapper.writeValueAsString(cvLinkDto);

        String successMessage = "Your CV has been submitted";
        ApiRes<Object> apiRes = new ApiRes<>();
        apiRes.setCode(null);
        apiRes.setStatus(null);
        apiRes.setMessage(successMessage);
        apiRes.setData(null);
        //when
        when(apiResUtil.returnApiRes(any(), any(), any(), any()))
                .thenReturn(apiRes);
        //then
        mockMvc.perform(patch("/auth/send-verify-instructor")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpectAll(status().isOk(),
                        jsonPath("$.message").value(successMessage));
    }

    @Test
    void testRegenerateOtp_Success() throws Exception {
        //given
        String successMessage = "Regenerate OTP successfully. Please check your email to verify your account.";
        ApiRes<Object> apiRes = new ApiRes<>();
        apiRes.setCode(null);
        apiRes.setStatus(null);
        apiRes.setMessage(successMessage);
        apiRes.setData(null);
        //when
        when(apiResUtil.returnApiRes(any(), any(), any(), any()))
                .thenReturn(apiRes);
        //then
        mockMvc.perform(put("/auth/regenerate-otp")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("email", "test@test.com"))
                .andExpectAll(status().isOk(),
                        jsonPath("$.message").value(successMessage));
    }

    @Test
    void testChangePassword_Success() throws Exception {
        //given
        ChangePasswordDto changePasswordDto = new ChangePasswordDto();
        changePasswordDto.setCurrentPassword("123456");
        changePasswordDto.setNewPassword("654321");
        changePasswordDto.setConfirmNewPassword("654321");
        String json = objectMapper.writeValueAsString(changePasswordDto);

        String successMessage = "Change password successfully.";
        ApiRes<Object> apiRes = new ApiRes<>();
        apiRes.setCode(null);
        apiRes.setStatus(null);
        apiRes.setMessage(successMessage);
        apiRes.setData(null);
        //when
        when(apiResUtil.returnApiRes(any(), any(), any(), any()))
                .thenReturn(apiRes);
        //then
        mockMvc.perform(patch("/auth/change-password")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpectAll(status().isOk(),
                        jsonPath("$.message").value(successMessage));
    }

    @Test
    void testForgetPassword_Success() throws Exception {
        //given
        String successMessage = "Please check your email to reset your password.";
        ApiRes<Object> apiRes = new ApiRes<>();
        apiRes.setCode(null);
        apiRes.setStatus(null);
        apiRes.setMessage(successMessage);
        apiRes.setData(null);
        //when
        when(apiResUtil.returnApiRes(any(), any(), any(), any()))
                .thenReturn(apiRes);
        //then
        mockMvc.perform(get("/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("email", "test@test.com"))
                .andExpectAll(status().isOk(),
                        jsonPath("$.message").value(successMessage));
    }

    @Test
    void testResetPassword_Success() throws Exception {
        //given
        ResetPasswordDto resetPasswordDto = new ResetPasswordDto();
        resetPasswordDto.setPassword("123456");
        resetPasswordDto.setConfirmPassword("123456");
        String json = objectMapper.writeValueAsString(resetPasswordDto);

        String successMessage = "Reset password successfully. Please login with your new password.";
        ApiRes<Object> apiRes = new ApiRes<>();
        apiRes.setCode(null);
        apiRes.setStatus(null);
        apiRes.setMessage(successMessage);
        apiRes.setData(null);
        //when
        when(apiResUtil.returnApiRes(any(), any(), any(), any()))
                .thenReturn(apiRes);
        //then
        mockMvc.perform(put("/auth/reset-password")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json)
                        .param("otp", "otp")
                        .param("email", "test@test.com"))
                .andExpectAll(status().isOk(),
                        jsonPath("$.message").value(successMessage));
    }
}
