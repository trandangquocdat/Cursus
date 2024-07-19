package com.fpt.cursus.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fpt.cursus.dto.request.ChangePasswordDto;
import com.fpt.cursus.dto.request.LoginReqDto;
import com.fpt.cursus.dto.request.RegisterReqDto;
import com.fpt.cursus.dto.request.ResetPasswordDto;
import com.fpt.cursus.dto.response.LoginResDto;
import com.fpt.cursus.entity.Account;
import com.fpt.cursus.entity.Otp;
import com.fpt.cursus.enums.Gender;
import com.fpt.cursus.enums.InstructorStatus;
import com.fpt.cursus.enums.Role;
import com.fpt.cursus.enums.UserStatus;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.repository.AccountRepo;
import com.fpt.cursus.service.impl.AccountServiceImpl;
import com.fpt.cursus.util.AccountUtil;
import com.fpt.cursus.util.FileUtil;
import com.fpt.cursus.util.Regex;
import com.fpt.cursus.util.TokenHandler;
import com.google.common.net.HttpHeaders;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.*;

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
    private ObjectMapper objectMapper;


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
    private FileService fileService;

    @Mock
    private FileUtil fileUtil;

    @Mock
    private FirebaseAuth firebaseAuth;

    private HttpServletRequest mockRequest;

    private Account account;
    private Account instructorAccount;
    private RegisterReqDto registerReqDto;
    private LoginReqDto loginReqDto;
    private LoginResDto loginResDto;
    private Otp otp;
    private int otpExpiration = 300; // Example OTP expiration in seconds
    private MockMultipartFile mockAvatarFile;

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
        account.setSubscribingJson("[]");


        instructorAccount = new Account();
        instructorAccount.setUsername("testinstructor");
        instructorAccount.setEmail("testIn@example.com");
        instructorAccount.setFullName("Test Instructor");
        instructorAccount.setPhone("098765432");
        instructorAccount.setPassword("password");
        instructorAccount.setRole(Role.INSTRUCTOR);
        instructorAccount.setStatus(UserStatus.ACTIVE);
        instructorAccount.setCreatedDate(new Date());
        instructorAccount.setSubscribersJson("[]");

        mockAvatarFile = new MockMultipartFile("avatar", "avatar.png", "image/png", "dummy content".getBytes());

        registerReqDto = RegisterReqDto.builder()
                .username("testuser")
                .password("password")
                .email("test@example.com")
                .fullName("Test User")
                .phone("1234567890")
                .avatar(mockAvatarFile)
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
        otp.setValid(true); // Ensure OTP is initially valid

        mockRequest = mock(HttpServletRequest.class);

        reset(firebaseAuth);

    }


    @Test
    void testSubscribeInstructor_Success() throws Exception {

        //given
        instructorAccount.setId(1L);
        account.setId(2L);
        List<Long> subcribing = List.of(3L, 4L); //của user
        List<Long> subcriber = List.of(5L, 6L);  // của instructor
        String subcribingJson = objectMapper.writeValueAsString(subcribing);
        String subcriberJson = objectMapper.writeValueAsString(subcriber);
        account.setSubscribingJson(subcribingJson);
        instructorAccount.setSubscribersJson(subcriberJson);

        //when
        when(accountRepo.findById(instructorAccount.getId())).thenReturn(Optional.of(instructorAccount));
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        accountService.subscribeInstructor(instructorAccount.getId());
        List<Long> expectedSubcribingList = new ArrayList<>(subcribing);
        expectedSubcribingList.add(instructorAccount.getId());
        List<Long> expectedSubscriberList = new ArrayList<>(subcriber);
        expectedSubscriberList.add(account.getId());

        //then
        assertEquals(objectMapper.writeValueAsString(expectedSubcribingList), account.getSubscribingJson());
        assertEquals(objectMapper.writeValueAsString(expectedSubscriberList), instructorAccount.getSubscribersJson());
    }


    @Test
    void testGetSubscribersUsers_JsonException() throws Exception {
        //given
        instructorAccount.setId(1L);
        account.setId(2L);
        List<Long> subcribing = List.of(3L, 4L); //của user
        String invalidJsonForSubcriber = "invalid";
//        List<Long> subcriber = List.of(5L, 6L);  // của instructor

        String subcribingJson = objectMapper.writeValueAsString(subcribing);
//        String subcriberJson = objectMapper.writeValueAsString(invalidJsonForSubcriber);
        account.setSubscribingJson(subcribingJson);
        instructorAccount.setSubscribersJson(invalidJsonForSubcriber);

        //when
        when(accountRepo.findById(instructorAccount.getId())).thenReturn(Optional.of(instructorAccount));
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        doThrow(new JsonProcessingException("Invalid JSON") {}).when(objectMapper).readValue(eq(invalidJsonForSubcriber), any(TypeReference.class));

        //then
        AppException exception = assertThrows(AppException.class, () -> {
            accountService.subscribeInstructor(instructorAccount.getId());
        });
        assertEquals(ErrorCode.PROCESS_ADD_STUDIED_COURSE_FAIL, exception.getErrorCode());
    }

    @Test
    void testGetSubscribingsUsers_JsonException() throws Exception {
        //given
        instructorAccount.setId(1L);
        account.setId(2L);
        String invalidJsonForSubcribing = "invalid";
        List<Long> subcriber = List.of(5L, 6L);
        String subcriberJson = objectMapper.writeValueAsString(subcriber);
        account.setSubscribingJson(invalidJsonForSubcribing);
        instructorAccount.setSubscribersJson(subcriberJson);

        //when
        when(accountRepo.findById(instructorAccount.getId())).thenReturn(Optional.of(instructorAccount));
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        doThrow(new JsonProcessingException("Invalid JSON") {}).when(objectMapper).readValue(eq(invalidJsonForSubcribing), any(TypeReference.class));

        //then
        AppException exception = assertThrows(AppException.class, () -> {
            accountService.subscribeInstructor(instructorAccount.getId());
        });
        assertEquals(ErrorCode.PROCESS_ADD_STUDIED_COURSE_FAIL, exception.getErrorCode());
    }

    @Test
    void testGetSubscribingsAndSubcriberUsers_BeEmpty() throws Exception {

        //given
        instructorAccount.setId(1L);
        account.setId(2L);

        String emptysubcriberJson = "";
        String emptysubcribingJson =  "";
        account.setSubscribingJson(emptysubcribingJson);
        instructorAccount.setSubscribersJson(emptysubcriberJson);

        //when
        when(accountRepo.findById(instructorAccount.getId())).thenReturn(Optional.of(instructorAccount));
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        accountService.subscribeInstructor(instructorAccount.getId());
        List<Long> expectedSubscriberList = new ArrayList<>();
        expectedSubscriberList.add(account.getId());
        List<Long> expectedSubscribingList = new ArrayList<>();
        expectedSubscribingList.add(instructorAccount.getId());
        //then
        assertEquals(objectMapper.writeValueAsString(expectedSubscribingList), account.getSubscribingJson());
        assertEquals(objectMapper.writeValueAsString(expectedSubscriberList), instructorAccount.getSubscribersJson());
    }


    @Test
    void testUnsubscribeInstructor_Success() throws Exception {

        //given
        instructorAccount.setId(1L);
        account.setId(2L);
        List<Long> subcribing = List.of(1L, 3L); //của user
        List<Long> subcriber = List.of(2L, 4L);  // của instructor
        String subcribingJson = objectMapper.writeValueAsString(subcribing);
        String subcriberJson = objectMapper.writeValueAsString(subcriber);
        account.setSubscribingJson(subcribingJson);
        instructorAccount.setSubscribersJson(subcriberJson);

        //when
        when(accountRepo.findById(instructorAccount.getId())).thenReturn(Optional.of(instructorAccount));
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        accountService.unsubscribeInstructor(instructorAccount.getId());
        List<Long> expectedSubcribingList = new ArrayList<>(subcribing);
        expectedSubcribingList.remove(instructorAccount.getId());
        List<Long> expectedSubscriberList = new ArrayList<>(subcriber);
        expectedSubscriberList.remove(account.getId());

        //then
        assertEquals(objectMapper.writeValueAsString(expectedSubcribingList), account.getSubscribingJson());
        assertEquals(objectMapper.writeValueAsString(expectedSubscriberList), instructorAccount.getSubscribersJson());

    }
}

//    @Test
//    void saveSubscribersUsers_JsonProcessingException() throws Exception {
//        // Given
//        instructorAccount.setId(1L);
//        account.setId(2L);
//
//        // Setup mocks
//        when(accountRepo.findById(account.getId())).thenReturn(Optional.of(account));
//        when(accountUtil.getCurrentAccount()).thenReturn(account);
//        when(objectMapper.writeValueAsString(anyList())).thenThrow(JsonProcessingException.class);
//
//        // When
//        AppException thrownException = assertThrows(AppException.class, () -> {
//            accountService.subscribeInstructor(account.getId());
//        });
//
//        // Then
//        assertEquals(ErrorCode.PROCESS_ADD_STUDIED_COURSE_FAIL, thrownException.getErrorCode());
//        verify(accountRepo, times(0)).save(any(Account.class));
//    }
//
//
//    public void testGetSubscribersUsers_InvalidJson() throws Exception {
//        // Setup
//        Account account = new Account();
//        account.setSubscribersJson("[1,2,3"); // Invalid JSON data
//
//        // Mock ObjectMapper behavior
//        when(objectMapper.readValue(account.getSubscribersJson(), new TypeReference<List<Long>>() {}))
//                .thenThrow(new JsonProcessingException("Invalid JSON") {});
//
//        // Test
//
//    }
//
//    @Test
//    void testUnsubscribeInstructor_Success() throws Exception {
//        //given
//        instructorAccount.setId(1L);
//        account.setId(2L);
//
//        //when
//        when(accountRepo.findById(instructorAccount.getId())).thenReturn(Optional.of(instructorAccount));
//        when(accountUtil.getCurrentAccount()).thenReturn(account);
//        when(objectMapper.readValue(anyString(), any(TypeReference.class)))
//                .thenReturn(new ArrayList<>());
//        accountService.unsubscribeInstructor(instructorAccount.getId());
//
//        //then
//        verify(accountRepo, times(2)).save(instructorAccount);
//        verify(accountRepo, times(2)).save(account);
//    }
//
//
//}




//    @Test
//    void testRegister_Success() {
//        // when
//        when(regex.isPhoneValid(anyString())).thenReturn(true);
//        when(accountRepo.existsByUsername(anyString())).thenReturn(false);
//        when(accountRepo.existsByEmail(anyString())).thenReturn(false);
//        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
//        when(accountRepo.save(any(Account.class))).thenReturn(account);
//        when(fileUtil.isImage(any(MockMultipartFile.class))).thenReturn(true);
//        doNothing().when(fileService).setAvatar(any(MockMultipartFile.class), any(Account.class));
//
//        // given
//        Account newAccount = accountService.register(registerReqDto);
//
//        // then
//        assertNotNull(newAccount);
//        assertEquals(registerReqDto.getUsername(), newAccount.getUsername());
//        assertEquals(registerReqDto.getEmail(), newAccount.getEmail());
//        assertEquals(UserStatus.INACTIVE, newAccount.getStatus());
//        assertEquals(Role.STUDENT, newAccount.getRole());
//
//        verify(accountRepo, times(1)).save(any(Account.class));
//    }
//
//    @Test
//    void testRegister_UsernameExists() {
//        // when
//        when(regex.isPhoneValid(anyString())).thenReturn(true);
//        when(accountRepo.existsByUsername(anyString())).thenReturn(true);
//
//        // given
//        AppException exception = assertThrows(AppException.class, () -> {
//            accountService.register(registerReqDto);
//        });
//
//        // then
//        assertEquals(ErrorCode.USERNAME_EXISTS, exception.getErrorCode());
//        verify(accountRepo, times(1)).existsByUsername(registerReqDto.getUsername());
//        verify(accountRepo, never()).save(any(Account.class));
//    }
//
//    @Test
//    void testRegister_EmailExists() {
//        // when
//        when(regex.isPhoneValid(anyString())).thenReturn(true);
//        when(accountRepo.existsByEmail(anyString())).thenReturn(true);
//
//        // given
//        AppException exception = assertThrows(AppException.class, () -> {
//            accountService.register(registerReqDto);
//        });
//
//        // then
//        assertEquals(ErrorCode.EMAIL_EXISTS, exception.getErrorCode());
//        verify(accountRepo, times(1)).existsByEmail(registerReqDto.getEmail());
//        verify(accountRepo, never()).save(any(Account.class));
//    }
//
//    @Test
//    void testRegister_PhoneNotValid() {
//        // when
//        when(regex.isPhoneValid(anyString())).thenReturn(false);
//
//        // given
//        AppException exception = assertThrows(AppException.class, () -> {
//            accountService.register(registerReqDto);
//        });
//
//        // then
//        assertEquals(ErrorCode.PHONE_NOT_VALID, exception.getErrorCode());
//        verify(accountRepo, never()).save(any(Account.class));
//    }
//
//    @Test
//    void testRegister_ImageInvalid() {
//        // when
//        when(regex.isPhoneValid(anyString())).thenReturn(true);
//        when(accountRepo.existsByUsername(anyString())).thenReturn(false);
//        when(accountRepo.existsByEmail(anyString())).thenReturn(false);
//        when(fileUtil.isImage(any(MockMultipartFile.class))).thenReturn(false); // Simulate invalid image
//
//        // given
//        AppException exception = assertThrows(AppException.class, () -> {
//            accountService.register(registerReqDto);
//        });
//
//        // then
//        assertEquals(ErrorCode.FILE_INVALID_IMAGE, exception.getErrorCode());
//        verify(accountRepo, never()).save(any(Account.class));
//    }
//
//
//    @Test
//    void testLogin() {
//        //when
//        Authentication authentication = mock(Authentication.class);
//        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
//                .thenReturn(authentication);
//
//        Account mockAccount = new Account();
//        mockAccount.setStatus(UserStatus.ACTIVE); // Set the status to ACTIVE
//        when(authentication.getPrincipal()).thenReturn(mockAccount);
//
//        when(tokenHandler.generateAccessToken(any(Account.class))).thenReturn("accessToken");
//        when(tokenHandler.generateRefreshToken(any(Account.class))).thenReturn("refreshToken");
//
//        //given
//        LoginResDto result = accountService.login(loginReqDto);
//
//        //then
//        assertNotNull(result);
//        assertEquals("accessToken", result.getAccessToken());
//        assertEquals("refreshToken", result.getRefreshToken());
//    }
//
//    @Test
//    void testLogin_BadCredentials() {
//        //when
//        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
//                .thenThrow(BadCredentialsException.class);
//
//        //given
//        AppException exception = assertThrows(AppException.class, () -> {
//            accountService.login(loginReqDto);
//        });
//
//        //then
//        assertEquals(ErrorCode.PASSWORD_NOT_CORRECT, exception.getErrorCode());
//    }
//
//    @Test
//    void testLogin_AccountInactive() {
//        // Given
//        LoginReqDto loginReqDto = new LoginReqDto();
//        loginReqDto.setUsername("testuser");
//        loginReqDto.setPassword("password");
//
//        Account account = new Account();
//        account.setUsername("testuser");
//        account.setPassword("password");
//        account.setStatus(UserStatus.INACTIVE);
//
//        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
//                .thenReturn(new UsernamePasswordAuthenticationToken(account, "password"));
//
//        // When
//        AppException exception = assertThrows(AppException.class, () -> {
//            accountService.login(loginReqDto);
//        });
//
//        // Then
//        assertEquals(ErrorCode.EMAIL_UNAUTHENTICATED, exception.getErrorCode());
//
//        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
//        verify(tokenHandler, never()).generateAccessToken(any(Account.class));
//        verify(tokenHandler, never()).generateRefreshToken(any(Account.class));
//        verifyNoMoreInteractions(authenticationManager, tokenHandler);
//    }
//
//
//    @Test
//    void testAuthenticateAccount_Success() {
//        //when
//        when(otpService.findOtpByEmailAndValid(anyString(), anyBoolean())).thenReturn(otp);
//        when(accountRepo.findByEmail(anyString())).thenReturn(Optional.of(account));
//        when(accountRepo.save(any(Account.class))).thenReturn(account);
//
//        //given
//        assertDoesNotThrow(() -> {
//            accountService.authenticateAccount("test@example.com", "123456");
//        });
//
//        //then
//        assertEquals(UserStatus.ACTIVE, account.getStatus());
//    }
//
//    @Test
//    void testAuthenticateAccount_InvalidOtp() {
//        //when
//        when(otpService.findOtpByEmailAndValid(anyString(), anyBoolean())).thenReturn(otp);
//
//        //given
//        AppException exception = assertThrows(AppException.class, () -> {
//            accountService.authenticateAccount("test@example.com", "654321");
//        });
//
//        //then
//        assertEquals(ErrorCode.OTP_INVALID, exception.getErrorCode());
//    }
//
//    @Test
//    void testChangePassword_Success() {
//        //when
//        when(accountUtil.getCurrentAccount()).thenReturn(account);
//        when(passwordEncoder.matches(eq("password"), anyString())).thenReturn(true);
//        when(passwordEncoder.matches(eq("newPassword"), anyString())).thenReturn(false);
//        when(passwordEncoder.encode(anyString())).thenReturn("encodedNewPassword");
//
//        //given
//        ChangePasswordDto changePasswordDto = new ChangePasswordDto();
//        changePasswordDto.setCurrentPassword("password");
//        changePasswordDto.setNewPassword("newPassword");
//        changePasswordDto.setConfirmNewPassword("newPassword");
//
//        assertDoesNotThrow(() -> {
//            accountService.changePassword(changePasswordDto);
//        });
//
//        //then
//        verify(accountRepo, times(1)).save(any(Account.class));
//    }
//
//    @Test
//    void testChangePassword_WrongCurrentPassword() {
//        //when
//        when(accountUtil.getCurrentAccount()).thenReturn(account);
//        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
//
//        //given
//        ChangePasswordDto changePasswordDto = new ChangePasswordDto();
//        changePasswordDto.setCurrentPassword("wrongPassword");
//        changePasswordDto.setNewPassword("newPassword");
//        changePasswordDto.setConfirmNewPassword("newPassword");
//
//        AppException exception = assertThrows(AppException.class, () -> {
//            accountService.changePassword(changePasswordDto);
//        });
//
//        //then
//        assertEquals(ErrorCode.PASSWORD_NOT_CORRECT, exception.getErrorCode());
//    }
//
//    @Test
//    void testForgotPassword() {
//        //when
//        when(accountRepo.findByEmail(anyString())).thenReturn(Optional.of(account));
//        doNothing().when(otpService).updateOldOtps(anyString());
//        doNothing().when(otpService).sendResetPasswordEmail(anyString(), anyString());
//        when(otpService.generateOtp()).thenReturn("123456");
//
//        //given
//        accountService.forgotPassword("test@example.com");
//
//        //then
//        verify(otpService, times(1)).sendResetPasswordEmail(anyString(), anyString());
//    }
//
//    @Test
//    void testForgotPassword_EmailNotFound() {
//        //when
//        when(accountRepo.findByEmail(anyString())).thenReturn(Optional.empty());
//
//        //given
//        AppException exception = assertThrows(AppException.class, () -> {
//            accountService.forgotPassword("nonexistent@example.com");
//        });
//
//        //then
//        assertEquals(ErrorCode.EMAIL_NOT_FOUND, exception.getErrorCode());
//    }
//
//    @Test
//    void testResetPassword_Valid() {
//        // Given
//        String newPassword = "newPassword";
//        String encodedPassword = "encodedPassword";
//
//        Otp otp = new Otp();
//        otp.setOtpGeneratedTime(LocalDateTime.now().minusMinutes(1)); // Set a valid generated time
//        otp.setOtp("123456");
//        when(otpService.findOtpByEmailAndValid(anyString(), anyBoolean())).thenReturn(otp);
//        when(accountRepo.findByEmail(anyString())).thenReturn(Optional.of(account));
//        when(passwordEncoder.encode(anyString())).thenReturn(encodedPassword);
//        when(accountRepo.save(any(Account.class))).thenReturn(account);
//
//        ResetPasswordDto resetPasswordDto = new ResetPasswordDto();
//        resetPasswordDto.setPassword(newPassword);
//        resetPasswordDto.setConfirmPassword(newPassword);
//
//        // When & Then
//        assertDoesNotThrow(() -> {
//            accountService.resetPassword("test@example.com", "123456", resetPasswordDto);
//        });
//
//        assertEquals(encodedPassword, account.getPassword());
//    }
//
//
//    @Test
//    void testResetPassword_InvalidOtp() {
//        //when
//        ResetPasswordDto resetPasswordDto = new ResetPasswordDto();
//        resetPasswordDto.setPassword(null); // Ensuring getPassword() returns null
//
//        //given
//        NullPointerException exception = assertThrows(NullPointerException.class, () -> {
//            accountService.resetPassword("test@example.com", "654321", resetPasswordDto);
//        });
//
//        //then
//        assertNotNull(exception.getMessage());
//    }
//
//    @Test
//    void testSaveAccount() {
//        //when
//        Account account = new Account();
//        account.setUsername("testUser");
//
//        //given
//        accountService.saveAccount(account);
//
//        //then
//        verify(accountRepo, times(1)).save(account);
//    }
//
//    @Test
//    void testGetAccountByUsername_Found() {
//        //when
//        String username = "testUser";
//        Account account = new Account();
//        account.setUsername(username);
//        when(accountRepo.findByUsername(username)).thenReturn(Optional.of(account));
//
//        Account foundAccount = accountService.getAccountByUsername(username);
//
//        //given
//        assertEquals(username, foundAccount.getUsername());
//
//        //then
//        verify(accountRepo, times(1)).findByUsername(username);
//    }
//
//    @Test
//    void testGetAccountByUsername_NotFound() {
//        //when
//        String username = "nonexistentUser";
//        when(accountRepo.findByUsername(username)).thenReturn(Optional.empty());
//
//        //given
//        AppException exception = assertThrows(AppException.class, () -> {
//            accountService.getAccountByUsername(username);
//        });
//
//        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
//
//        //then
//        verify(accountRepo, times(1)).findByUsername(username);
//    }
//
//    @Test
//    void testValidateOtp_ValidOtp() {
//        // Given
//        otp.setOtpGeneratedTime(LocalDateTime.now().minusSeconds(60)); // Set a valid time
//
//        // Mocking behavior for findOtpByEmailAndValid
////        when(otpService.findOtpByEmailAndValid(anyString(), anyBoolean())).thenReturn(otp);
//
//        // When
//        boolean result = accountService.validateOtp(otp, "123456");
//
//        // Then
//        assertTrue(result);
//        assertTrue(otp.getValid()); // Ensure OTP remains valid
//        verify(otpService, never()).saveOtp(anyString(), anyString()); // Verify saveOtp is never called
//    }
//
//
//
//    @Test
//    void testValidateOtp_ExpiredOtp() {
//        // Given
//        otp.setOtpGeneratedTime(LocalDateTime.now().minusSeconds(otpExpiration + 10)); // Set an expired time
//
//        // When
//        AppException exception = assertThrows(AppException.class, () -> {
//            accountService.validateOtp(otp, "123456");
//        });
//
//        // Then
//        assertEquals(ErrorCode.OTP_EXPIRED, exception.getErrorCode());
//        assertFalse(otp.getValid()); // Ensure OTP is marked as invalid
//        verify(otpService, times(1)).saveOtp(otp.getEmail(), otp.getOtp()); // Verify saveOtp is called once
//    }
//
//    @Test
//    void testSetAdmin_UserExists() {
//        String username = "testUser";
//        Account currentUser = new Account();
//        currentUser.setUsername("currentAdmin");
//
//        when(accountRepo.findByUsername(username)).thenReturn(Optional.of(account));
//        when(accountUtil.getCurrentAccount()).thenReturn(currentUser);
//
//        accountService.setAdmin(username);
//
//        assertEquals(Role.ADMIN, account.getRole());
//        assertEquals(UserStatus.ACTIVE, account.getStatus());
//        assertEquals("currentAdmin", account.getUpdatedBy());
//
//        verify(accountRepo, times(1)).save(account);
//    }
//
//    @Test
//    void testSetAdmin_UserNotFound() {
//        //when
//        String username = "nonExistentUser";
//
//        when(accountRepo.findByUsername(username)).thenReturn(Optional.empty());
//
//        //given
//        AppException exception = assertThrows(AppException.class, () -> {
//            accountService.setAdmin(username);
//        });
//
//        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
//
//        //then
//        verify(accountRepo, never()).save(any(Account.class));
//    }
//
//    @Test
//    void testApproveInstructorById_UserExists_Approved() {
//        //when
//        Long userId = 1L;
//        account.setRole(Role.STUDENT);
//        account.setInstructorStatus(InstructorStatus.WAITING);
//        when(accountRepo.findById(userId)).thenReturn(Optional.of(account));
//
//        //given
//        accountService.approveInstructorById(userId, InstructorStatus.APPROVED);
//        assertEquals(Role.INSTRUCTOR, account.getRole());
//        assertEquals(InstructorStatus.APPROVED, account.getInstructorStatus());
//
//        //then
//        verify(accountRepo, times(1)).save(account);
//    }
//
//    @Test
//    void testApproveInstructorById_UserExists_Rejected() {
//        //when
//        Long userId = 1L;
//        account.setInstructorStatus(InstructorStatus.WAITING);
//        when(accountRepo.findById(userId)).thenReturn(Optional.of(account));
//
//        //given
//        accountService.approveInstructorById(userId, InstructorStatus.REJECTED);
//        assertEquals(InstructorStatus.REJECTED, account.getInstructorStatus());
//
//        //then
//        verify(accountRepo, times(1)).save(account);
//    }
//
//    @Test
//    void testVerifyInstructorById_UserNotFound() {
//        //when
//        Long userId = 1L;
//        when(accountRepo.findById(userId)).thenReturn(Optional.empty());
//
//        //given
//        AppException exception = assertThrows(AppException.class, () -> {
//            accountService.approveInstructorById(userId, InstructorStatus.APPROVED);
//        });
//        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
//
//        //then
//        verify(accountRepo, never()).save(any(Account.class));
//    }
//
//    @Test
//    void testSendCv() throws IOException {
//        MockMultipartFile mockFile = new MockMultipartFile("cv", "cv.pdf", "application/pdf", "CV content".getBytes());
//
//        when(accountUtil.getCurrentAccount()).thenReturn(account);
//        when(fileUtil.isPDF(mockFile)).thenReturn(true);
//        when(fileService.uploadFile(mockFile)).thenReturn("http://example.com/cv");
//
//        accountService.sendCv(mockFile);
//
//        assertEquals("http://example.com/cv", account.getCvLink());
//        assertEquals(InstructorStatus.WAITING, account.getInstructorStatus());
//
//        verify(accountRepo, times(1)).save(account);
//    }
//
//    @Test
//    void testSendCv_InvalidFile() throws IOException {
//        MockMultipartFile mockFile = new MockMultipartFile("cv", "cv.txt", "text/plain", "CV content".getBytes());
//
//        when(accountUtil.getCurrentAccount()).thenReturn(account);
//        when(fileUtil.isPDF(mockFile)).thenReturn(false);
//
//        AppException exception = assertThrows(AppException.class, () -> accountService.sendCv(mockFile));
//        assertEquals(ErrorCode.FILE_INVALID_PDF, exception.getErrorCode());
//
//        verify(accountRepo, never()).save(account);
//    }
//
//    @Test
//    void testSendCv_FileUploadFail() throws IOException {
//        MockMultipartFile mockFile = new MockMultipartFile("cv", "cv.pdf", "application/pdf", "CV content".getBytes());
//
//        when(accountUtil.getCurrentAccount()).thenReturn(account);
//        when(fileUtil.isPDF(mockFile)).thenReturn(true);
//        when(fileService.uploadFile(mockFile)).thenThrow(new IOException("Upload failed"));
//
//        AppException exception = assertThrows(AppException.class, () -> accountService.sendCv(mockFile));
//        assertEquals(ErrorCode.FILE_UPLOAD_FAIL, exception.getErrorCode());
//
//        verify(accountRepo, never()).save(account);
//    }
//
//    @Test
//    void testGetInstructorByInstStatus_Found() {
//        //when
//        InstructorStatus status = InstructorStatus.APPROVED;
//        Account instructor = new Account();
//        instructor.setInstructorStatus(status);
//
//        when(accountRepo.findAccountByInstructorStatus(status)).thenReturn(List.of(instructor));
//
//        //given
//        List<Account> result = accountService.getInstructorByInstStatus(status);
//
//        assertFalse(result.isEmpty());
//
//        //then
//        assertEquals(status, result.get(0).getInstructorStatus());
//    }
//
//    @Test
//    void testGetInstructorByInstStatus_NotFound() {
//        //when
//        InstructorStatus status = InstructorStatus.APPROVED;
//        when(accountRepo.findAccountByInstructorStatus(status)).thenReturn(List.of());
//
//        //given
//        AppException exception = assertThrows(AppException.class, () -> {
//            accountService.getInstructorByInstStatus(status);
//        });
//
//        //then
//        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
//    }
//
//    @Test
//    void testGetInstructorByName_Found() {
//        // given
//        String name = "John Doe";
//        Account instructor = new Account();
//        instructor.setFullName("John Doe");
//        instructor.setInstructorStatus(InstructorStatus.APPROVED);
//
//        when(accountRepo.findByFullNameLikeAndInstructorStatus("%" + name + "%", InstructorStatus.APPROVED))
//                .thenReturn(List.of(instructor));
//
//        // when
//        List<Account> result = accountService.getInstructorByName(name);
//
//        // then
//        assertFalse(result.isEmpty());
//        assertEquals("John Doe", result.get(0).getFullName());
//        assertEquals(InstructorStatus.APPROVED, result.get(0).getInstructorStatus());
//    }
//
//    @Test
//    void testGetInstructorByName_NotFound() {
//        // given
//        String name = "Nonexistent";
//        when(accountRepo.findByFullNameLikeAndInstructorStatus("%" + name + "%", InstructorStatus.APPROVED))
//                .thenReturn(List.of());
//
//        // when + then
//        AppException exception = assertThrows(AppException.class, () -> {
//            accountService.getInstructorByName(name);
//        });
//
//        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
//    }
//
//
//    @Test
//    void testRegenerateOtp_UserExists() {
//        //when
//        String email = "test@example.com";
//        when(accountRepo.findByEmail(email)).thenReturn(Optional.of(account));
//        when(otpService.generateOtp()).thenReturn("123456");
//
//        //given
//        accountService.regenerateOtp(email);
//
//        //then
//        verify(otpService, times(1)).updateOldOtps(email);
//        verify(otpService, times(1)).sendOtpEmail(email, "123456");
//        verify(otpService, times(1)).saveOtp(email, "123456");
//    }
//
//    @Test
//    void testRegenerateOtp_UserNotFound() {
//        //when
//        String email = "nonexistent@example.com";
//
//        when(accountRepo.findByEmail(email)).thenReturn(Optional.empty());
//
//        //given
//        AppException exception = assertThrows(AppException.class, () -> {
//            accountService.regenerateOtp(email);
//        });
//
//        assertEquals(ErrorCode.EMAIL_NOT_FOUND, exception.getErrorCode());
//
//        //then
//        verify(otpService, never()).updateOldOtps(anyString());
//        verify(otpService, never()).sendOtpEmail(anyString(), anyString());
//        verify(otpService, never()).saveOtp(anyString(), anyString());
//    }
//
//    @Test
//    void testRefreshToken_Valid() {
//        //when
//        String token = "validAccessToken";
//        String username = "testuser";
//        when(mockRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
//        when(tokenHandler.getInfoByToken(token)).thenReturn(username);
//
//        Account mockAccount = new Account();
//        mockAccount.setUsername(username);
//        when(accountRepo.findAccountByUsername(username)).thenReturn(mockAccount);
//
//        when(tokenHandler.generateAccessToken(mockAccount)).thenReturn("newAccessToken");
//        when(tokenHandler.generateRefreshToken(mockAccount)).thenReturn("newRefreshToken");
//
//        //given
//        LoginResDto result = accountService.refreshToken(mockRequest);
//
//        assertEquals("newAccessToken", result.getAccessToken());
//        assertEquals("newRefreshToken", result.getRefreshToken());
//
//        //then
//        verify(mockRequest).getHeader(HttpHeaders.AUTHORIZATION);
//        verify(tokenHandler).getInfoByToken(token);
//        verify(accountRepo).findAccountByUsername(username);
//        verify(tokenHandler).generateAccessToken(mockAccount);
//        verify(tokenHandler).generateRefreshToken(mockAccount);
//    }
//
//    @Test
//    void testRefreshToken_InvalidAuthHeader() {
//        //when
//        when(mockRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);
//
//        //given
//        AppException exception = assertThrows(AppException.class, () -> {
//            accountService.refreshToken(mockRequest);
//        });
//
//        assertEquals(ErrorCode.REFRESH_TOKEN_NOT_VALID, exception.getErrorCode());
//
//        //then
//        verify(mockRequest).getHeader(HttpHeaders.AUTHORIZATION);
//        verify(tokenHandler, never()).getInfoByToken(anyString());
//        verify(accountRepo, never()).findAccountByUsername(anyString());
//        verify(tokenHandler, never()).generateAccessToken(any(Account.class));
//        verify(tokenHandler, never()).generateRefreshToken(any(Account.class));
//    }
//
//    @Test
//    void testRefreshToken_AccountNotFound() {
//        //when
//        String token = "validAccessToken";
//        String username = "nonexistentUser";
//
//        when(mockRequest.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer " + token);
//
//        when(tokenHandler.getInfoByToken(token)).thenReturn(username);
//
//        when(accountRepo.findAccountByUsername(username)).thenReturn(null);
//
//        //given
//        AppException exception = assertThrows(AppException.class, () -> {
//            accountService.refreshToken(mockRequest);
//        });
//
//        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
//
//        //then
//        verify(mockRequest).getHeader(HttpHeaders.AUTHORIZATION);
//        verify(tokenHandler).getInfoByToken(token);
//        verify(accountRepo).findAccountByUsername(username);
//        verify(tokenHandler, never()).generateAccessToken(any(Account.class));
//        verify(tokenHandler, never()).generateRefreshToken(any(Account.class));
//    }
//
//}
//
