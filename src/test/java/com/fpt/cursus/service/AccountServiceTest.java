package com.fpt.cursus.service;

import com.fpt.cursus.dto.request.*;
import com.fpt.cursus.dto.response.LoginResDto;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Otp;
import com.fpt.cursus.enums.status.UserStatus;
import com.fpt.cursus.enums.type.Gender;
import com.fpt.cursus.enums.type.InstructorStatus;
import com.fpt.cursus.enums.type.Role;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.repository.AccountRepo;
import com.fpt.cursus.service.impl.AccountServiceImpl;
import com.fpt.cursus.util.AccountUtil;
import com.fpt.cursus.util.Regex;
import com.fpt.cursus.util.TokenHandler;
import com.google.common.net.HttpHeaders;
import com.google.firebase.auth.FirebaseAuth;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @InjectMocks
    private AccountServiceImpl accountService;

    @Mock
    private AccountRepo accountRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenHandler tokenHandler;

    @Mock
    private AccountUtil accountUtil;

    @Mock
    private Regex regex;

    @Mock
    private OtpService otpService;

    @Mock
    private FirebaseAuth firebaseAuth;

    private HttpServletRequest mockRequest;

    private Account account;
    private RegisterReqDto registerReqDto;
    private LoginReqDto loginReqDto;
    private LoginResDto loginResDto;
    private Otp otp;

    @BeforeEach
    void setUp() {
        account = new Account();
        account.setUsername("testuser");
        account.setEmail("test@example.com");
        account.setFullName("Test User");
        account.setPhone("0123456789");
        account.setPassword("password");
        account.setRole(Role.STUDENT);
        account.setStatus(UserStatus.INACTIVE);
        account.setCreatedDate(new Date());

        registerReqDto = RegisterReqDto.builder()
                .username("testuser")
                .password("password")
                .email("test@example.com")
                .fullName("Test User")
                .phone("1234567890")
                .avatar("avatar.png")
                .gender(Gender.MALE)
                .build();

        loginReqDto = new LoginReqDto();
        loginReqDto.setUsername("testuser");
        loginReqDto.setPassword("password");

        loginResDto = new LoginResDto();
        loginResDto.setAccessToken("accessToken");
        loginResDto.setRefreshToken("refreshToken");

        otp = new Otp();
        otp.setEmail("test@example.com");
        otp.setOtp("123456");
        otp.setOtpGeneratedTime(LocalDateTime.now()); // Ensure otpGeneratedTime is set

        mockRequest = mock(HttpServletRequest.class);
        reset(firebaseAuth);
    }

    @Test
    void testRegister() {
        //when
        when(regex.isPhoneValid(anyString())).thenReturn(true);
        when(accountRepo.existsByUsername(anyString())).thenReturn(false);
        when(accountRepo.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(accountRepo.save(any(Account.class))).thenReturn(account);

        //given
        Account newAccount = accountService.register(registerReqDto);

        //then
        assertNotNull(newAccount);
        assertEquals(registerReqDto.getUsername(), newAccount.getUsername());
        assertEquals(registerReqDto.getEmail(), newAccount.getEmail());
    }

    @Test
    void testRegister_UsernameExists() {
        // when
        when(accountRepo.existsByUsername(anyString())).thenReturn(true);

        // given
        AppException exception = assertThrows(AppException.class, () -> {
            accountService.register(registerReqDto);
        });

        // then
        assertEquals(ErrorCode.USERNAME_EXISTS, exception.getErrorCode());
        verify(accountRepo, times(1)).existsByUsername(registerReqDto.getUsername());
        verify(accountRepo, never()).save(any(Account.class));
    }

    @Test
    void testLogin() {
        //when
        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        Account mockAccount = new Account();
        mockAccount.setStatus(UserStatus.ACTIVE); // Set the status to ACTIVE
        when(authentication.getPrincipal()).thenReturn(mockAccount);

        when(tokenHandler.generateAccessToken(any(Account.class))).thenReturn("accessToken");
        when(tokenHandler.generateRefreshToken(any(Account.class))).thenReturn("refreshToken");

        //given
        LoginResDto result = accountService.login(loginReqDto);

        //then
        assertNotNull(result);
        assertEquals("accessToken", result.getAccessToken());
        assertEquals("refreshToken", result.getRefreshToken());
    }

    @Test
    void testLogin_BadCredentials() {
        //when
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(BadCredentialsException.class);

        //given
        AppException exception = assertThrows(AppException.class, () -> {
            accountService.login(loginReqDto);
        });

        //then
        assertEquals(ErrorCode.PASSWORD_NOT_CORRECT, exception.getErrorCode());
    }

    @Test
    void testLogin_AccountInactive() {
        //when
        Account account = new Account();
        account.setUsername("testuser");
        account.setPassword("password");
        account.setStatus(UserStatus.INACTIVE);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(account, "password"));
        when(tokenHandler.generateAccessToken(any(Account.class))).thenReturn("accessToken");
        when(tokenHandler.generateRefreshToken(any(Account.class))).thenReturn("refreshToken");

        //given
        AppException exception = assertThrows(AppException.class, () -> {
            accountService.login(loginReqDto);
        });

        //then
        assertEquals(ErrorCode.EMAIL_UNAUTHENTICATED, exception.getErrorCode());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenHandler).generateAccessToken(any(Account.class));
        verifyNoMoreInteractions(authenticationManager, tokenHandler);
    }

    @Test
    void testVerifyAccount_Success() {
        //when
        when(otpService.findOtpByEmailAndValid(anyString(), anyBoolean())).thenReturn(otp);
        when(accountRepo.findByEmail(anyString())).thenReturn(Optional.of(account));
        when(accountRepo.save(any(Account.class))).thenReturn(account);

        //given
        assertDoesNotThrow(() -> {
            accountService.verifyAccount("test@example.com", "123456");
        });

        //then
        assertEquals(UserStatus.ACTIVE, account.getStatus());
    }

    @Test
    void testVerifyAccount_InvalidOtp() {
        //when
        when(otpService.findOtpByEmailAndValid(anyString(), anyBoolean())).thenReturn(otp);

        //given
        AppException exception = assertThrows(AppException.class, () -> {
            accountService.verifyAccount("test@example.com", "654321");
        });

        //then
        assertEquals(ErrorCode.OTP_INVALID, exception.getErrorCode());
    }

    @Test
    void testChangePassword_Success() {
        //when
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(passwordEncoder.matches(eq("password"), anyString())).thenReturn(true);
        when(passwordEncoder.matches(eq("newPassword"), anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedNewPassword");

        //given
        ChangePasswordDto changePasswordDto = new ChangePasswordDto();
        changePasswordDto.setCurrentPassword("password");
        changePasswordDto.setNewPassword("newPassword");
        changePasswordDto.setConfirmNewPassword("newPassword");

        assertDoesNotThrow(() -> {
            accountService.changePassword(changePasswordDto);
        });

        //then
        verify(accountRepo, times(1)).save(any(Account.class));
    }

    @Test
    void testChangePassword_WrongCurrentPassword() {
        //when
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        //given
        ChangePasswordDto changePasswordDto = new ChangePasswordDto();
        changePasswordDto.setCurrentPassword("wrongPassword");
        changePasswordDto.setNewPassword("newPassword");
        changePasswordDto.setConfirmNewPassword("newPassword");

        AppException exception = assertThrows(AppException.class, () -> {
            accountService.changePassword(changePasswordDto);
        });

        //then
        assertEquals(ErrorCode.PASSWORD_NOT_CORRECT, exception.getErrorCode());
    }

    @Test
    void testForgotPassword() {
        //when
        when(accountRepo.findByEmail(anyString())).thenReturn(Optional.of(account));
        doNothing().when(otpService).updateOldOtps(anyString());
        doNothing().when(otpService).sendResetPasswordEmail(anyString(), anyString());
        when(otpService.generateOtp()).thenReturn("123456");

        //given
        accountService.forgotPassword("test@example.com");

        //then
        verify(otpService, times(1)).sendResetPasswordEmail(anyString(), anyString());
    }

    @Test
    void testForgotPassword_EmailNotFound() {
        //when
        when(accountRepo.findByEmail(anyString())).thenReturn(Optional.empty());

        //given
        AppException exception = assertThrows(AppException.class, () -> {
            accountService.forgotPassword("nonexistent@example.com");
        });

        //then
        assertEquals(ErrorCode.EMAIL_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void testResetPassword_Valid() {
        //when
        String newPassword = "newPassword";
        String encodedPassword = "encodedPassword";

        Otp otp = new Otp();
        when(otpService.findOtpByEmailAndValid(anyString(), anyBoolean())).thenReturn(otp);
        when(accountRepo.findByEmail(anyString())).thenReturn(Optional.of(account));
        when(passwordEncoder.encode(anyString())).thenReturn(encodedPassword);
        when(accountRepo.save(any(Account.class))).thenReturn(account);

        //given
        ResetPasswordDto resetPasswordDto = new ResetPasswordDto();
        resetPasswordDto.setPassword(newPassword);
        resetPasswordDto.setConfirmPassword(newPassword);

        assertDoesNotThrow(() -> {
            accountService.resetPassword("test@example.com", "123456", resetPasswordDto);
        });

        //then
        assertEquals(encodedPassword, account.getPassword());
    }

    @Test
    void testResetPassword_InvalidOtp() {
        //when
        ResetPasswordDto resetPasswordDto = new ResetPasswordDto();
        resetPasswordDto.setPassword(null); // Ensuring getPassword() returns null

        //given
        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
            accountService.resetPassword("test@example.com", "654321", resetPasswordDto);
        });

        //then
        assertNotNull(exception.getMessage());
    }

    @Test
    void testSaveAccount() {
        //when
        Account account = new Account();
        account.setUsername("testUser");

        //given
        accountService.saveAccount(account);

        //then
        verify(accountRepo, times(1)).save(account);
    }

    @Test
    void testExistAdmin() {
        //when
        String username = "adminUser";
        when(accountRepo.existsByUsername(username)).thenReturn(true);

        //given
        boolean exists = accountService.existAdmin(username);
        assertTrue(exists);

        //then
        verify(accountRepo, times(1)).existsByUsername(username);
    }

    @Test
    void testGetAccountByUsername_Found() {
        //when
        String username = "testUser";
        Account account = new Account();
        account.setUsername(username);
        when(accountRepo.findByUsername(username)).thenReturn(Optional.of(account));

        Account foundAccount = accountService.getAccountByUsername(username);

        //given
        assertEquals(username, foundAccount.getUsername());

        //then
        verify(accountRepo, times(1)).findByUsername(username);
    }

    @Test
    void testGetAccountByUsername_NotFound() {
        //when
        String username = "nonexistentUser";
        when(accountRepo.findByUsername(username)).thenReturn(Optional.empty());

        //given
        AppException exception = assertThrows(AppException.class, () -> {
            accountService.getAccountByUsername(username);
        });

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());

        //then
        verify(accountRepo, times(1)).findByUsername(username);
    }

    @Test
    void testValidateOtp_ValidOtp() {
        //when
        otp.setOtpGeneratedTime(LocalDateTime.now().minusSeconds(60)); // Set a valid time

        //given
        boolean result = accountService.validateOtp(otp, "123456");

        //then
        assertTrue(result);
    }

    @Test
    void testValidateOtp_InvalidOtp() {
        //when
        otp.setOtpGeneratedTime(LocalDateTime.now().minusSeconds(60)); // Set a valid time

        //given
        boolean result = accountService.validateOtp(otp, "654321");

        //then
        assertFalse(result);
    }

    @Test
    void testValidateOtp_ExpiredOtp() {
        //when
        otp.setOtpGeneratedTime(LocalDateTime.now().minusMinutes(3)); // Set an expired time

        //given
        AppException exception = assertThrows(AppException.class, () -> {
            accountService.validateOtp(otp, "123456");
        });

        assertEquals(ErrorCode.OTP_EXPIRED, exception.getErrorCode());
        assertFalse(otp.getValid()); // Ensure the OTP is marked as invalid

        //then
        verify(otpService, times(1)).saveOtp(anyString(), anyString());
    }

    @Test
    void testSetAdmin_UserExists() {
        //when
        String username = "testUser";
        account.setRole(Role.STUDENT); // Initially not an admin
        when(accountRepo.findByUsername(username)).thenReturn(Optional.of(account));

        //given
        accountService.setAdmin(username);

        assertEquals(Role.ADMIN, account.getRole());

        //then
        verify(accountRepo, times(1)).save(account);
    }

    @Test
    void testSetAdmin_UserNotFound() {
        //when
        String username = "nonExistentUser";

        when(accountRepo.findByUsername(username)).thenReturn(Optional.empty());

        //given
        AppException exception = assertThrows(AppException.class, () -> {
            accountService.setAdmin(username);
        });

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());

        //then
        verify(accountRepo, never()).save(any(Account.class));
    }

    @Test
    void testDeleteAccount_UserExists() {
        //when
        when(accountRepo.findByUsername(anyString())).thenReturn(Optional.of(account));

        //given
        accountService.deleteAccount("testUser");
        assertEquals(UserStatus.DELETED, account.getStatus());

        //then
        verify(accountRepo, times(1)).save(account);
    }

    @Test
    void testDeleteAccount_UserNotFound() {
        //when
        when(accountRepo.findByUsername(anyString())).thenReturn(Optional.empty());

        //given
        AppException exception = assertThrows(AppException.class, () -> {
            accountService.deleteAccount("nonExistentUser");
        });
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());

        //then
        verify(accountRepo, never()).save(any(Account.class));
    }

    @Test
    void testVerifyInstructorById_UserExists_Approved() {
        //when
        Long userId = 1L;
        account.setRole(Role.STUDENT);
        account.setInstructorStatus(InstructorStatus.WAITING);
        when(accountRepo.findById(userId)).thenReturn(Optional.of(account));

        //given
        accountService.verifyInstructorById(userId, InstructorStatus.APPROVED);
        assertEquals(Role.INSTRUCTOR, account.getRole());
        assertEquals(InstructorStatus.APPROVED, account.getInstructorStatus());

        //then
        verify(accountRepo, times(1)).save(account);
    }

    @Test
    void testVerifyInstructorById_UserExists_Rejected() {
        //when
        Long userId = 1L;
        account.setInstructorStatus(InstructorStatus.WAITING);
        when(accountRepo.findById(userId)).thenReturn(Optional.of(account));

        //given
        accountService.verifyInstructorById(userId, InstructorStatus.REJECTED);
        assertEquals(InstructorStatus.REJECTED, account.getInstructorStatus());

        //then
        verify(accountRepo, times(1)).save(account);
    }

    @Test
    void testVerifyInstructorById_UserNotFound() {
        //when
        Long userId = 1L;
        when(accountRepo.findById(userId)).thenReturn(Optional.empty());

        //given
        AppException exception = assertThrows(AppException.class, () -> {
            accountService.verifyInstructorById(userId, InstructorStatus.APPROVED);
        });
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());

        //then
        verify(accountRepo, never()).save(any(Account.class));
    }

    @Test
    void testSendVerifyInstructor() {
        //when
        CvLinkDto cvLinkDto = new CvLinkDto();
        cvLinkDto.setCvLink("http://example.com/cv");
        when(accountUtil.getCurrentAccount()).thenReturn(account);

        //given
        accountService.sendVerifyInstructor(cvLinkDto);

        assertEquals("http://example.com/cv", account.getCvLink());
        assertEquals(InstructorStatus.WAITING, account.getInstructorStatus());

        //then
        verify(accountRepo, times(1)).save(account);
    }

    @Test
    void testGetInstructorByInstStatus_Found() {
        //when
        InstructorStatus status = InstructorStatus.APPROVED;
        Account instructor = new Account();
        instructor.setInstructorStatus(status);

        when(accountRepo.findAccountByInstructorStatus(status)).thenReturn(List.of(instructor));

        //given
        List<Account> result = accountService.getInstructorByInstStatus(status);

        assertFalse(result.isEmpty());

        //then
        assertEquals(status, result.get(0).getInstructorStatus());
    }

    @Test
    void testGetInstructorByInstStatus_NotFound() {
        //when
        InstructorStatus status = InstructorStatus.APPROVED;
        when(accountRepo.findAccountByInstructorStatus(status)).thenReturn(List.of());

        //given
        AppException exception = assertThrows(AppException.class, () -> {
            accountService.getInstructorByInstStatus(status);
        });

        //then
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void testRegenerateOtp_UserExists() {
        //when
        String email = "test@example.com";
        when(accountRepo.findByEmail(email)).thenReturn(Optional.of(account));
        when(otpService.generateOtp()).thenReturn("123456");

        //given
        accountService.regenerateOtp(email);

        //then
        verify(otpService, times(1)).updateOldOtps(email);
        verify(otpService, times(1)).sendOtpEmail(email, "123456");
        verify(otpService, times(1)).saveOtp(email, "123456");
    }

    @Test
    void testRegenerateOtp_UserNotFound() {
        //when
        String email = "nonexistent@example.com";

        when(accountRepo.findByEmail(email)).thenReturn(Optional.empty());

        //given
        AppException exception = assertThrows(AppException.class, () -> {
            accountService.regenerateOtp(email);
        });

        assertEquals(ErrorCode.EMAIL_NOT_FOUND, exception.getErrorCode());

        //then
        verify(otpService, never()).updateOldOtps(anyString());
        verify(otpService, never()).sendOtpEmail(anyString(), anyString());
        verify(otpService, never()).saveOtp(anyString(), anyString());
    }

    @Test
    void testRefreshToken_Valid() {
        //when
        String token = "validAccessToken";
        String username = "testuser";
        when(mockRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
        when(tokenHandler.getInfoByToken(token)).thenReturn(username);

        Account mockAccount = new Account();
        mockAccount.setUsername(username);
        when(accountRepo.findAccountByUsername(username)).thenReturn(mockAccount);

        when(tokenHandler.generateAccessToken(mockAccount)).thenReturn("newAccessToken");
        when(tokenHandler.generateRefreshToken(mockAccount)).thenReturn("newRefreshToken");

        //given
        LoginResDto result = accountService.refreshToken(mockRequest);

        assertEquals("newAccessToken", result.getAccessToken());
        assertEquals("newRefreshToken", result.getRefreshToken());

        //then
        verify(mockRequest).getHeader(HttpHeaders.AUTHORIZATION);
        verify(tokenHandler).getInfoByToken(token);
        verify(accountRepo).findAccountByUsername(username);
        verify(tokenHandler).generateAccessToken(mockAccount);
        verify(tokenHandler).generateRefreshToken(mockAccount);
    }

    @Test
    void testRefreshToken_InvalidAuthHeader() {
        //when
        when(mockRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        //given
        AppException exception = assertThrows(AppException.class, () -> {
            accountService.refreshToken(mockRequest);
        });

        assertEquals(ErrorCode.REFRESH_TOKEN_NOT_VALID, exception.getErrorCode());

        //then
        verify(mockRequest).getHeader(HttpHeaders.AUTHORIZATION);
        verify(tokenHandler, never()).getInfoByToken(anyString());
        verify(accountRepo, never()).findAccountByUsername(anyString());
        verify(tokenHandler, never()).generateAccessToken(any(Account.class));
        verify(tokenHandler, never()).generateRefreshToken(any(Account.class));
    }

    @Test
    void testRefreshToken_AccountNotFound() {
        //when
        String token = "validAccessToken";
        String username = "nonexistentUser";

        when(mockRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);

        when(tokenHandler.getInfoByToken(token)).thenReturn(username);

        when(accountRepo.findAccountByUsername(username)).thenReturn(null);

        //given
        AppException exception = assertThrows(AppException.class, () -> {
            accountService.refreshToken(mockRequest);
        });

        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());

        //then
        verify(mockRequest).getHeader(HttpHeaders.AUTHORIZATION);
        verify(tokenHandler).getInfoByToken(token);
        verify(accountRepo).findAccountByUsername(username);
        verify(tokenHandler, never()).generateAccessToken(any(Account.class));
        verify(tokenHandler, never()).generateRefreshToken(any(Account.class));
    }

}

