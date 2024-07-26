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
import com.fpt.cursus.util.*;
import com.google.common.net.HttpHeaders;
import com.google.firebase.auth.FirebaseAuth;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    private final int otpExpiration = 300; // Example OTP expiration in seconds
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
    private ModelMapper modelMapper;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private Regex regex;
    @Mock
    private OtpService otpService;
    @Mock
    private FileService fileService;
    @Mock
    private FileUtil fileUtil;
    @Mock
    private PageUtil pageUtil;
    @Mock
    private FirebaseAuth firebaseAuth;
    private HttpServletRequest mockRequest;
    private Account account;
    private RegisterReqDto registerReqDto;
    private LoginReqDto loginReqDto;
    private Account instructorAccount;

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

        MockMultipartFile mockAvatarFile = new MockMultipartFile("avatar", "avatar.png", "image/png", "dummy content".getBytes());

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

        mockRequest = mock(HttpServletRequest.class);
        reset(firebaseAuth);

    }

    @Test
    void testRegister_Success() {
        // Given
        RegisterReqDto registerReqDto = RegisterReqDto.builder()
                .username("testuser")
                .password("password123")
                .email("test@example.com")
                .fullName("Test User")
                .phone("1234567890")
                .gender(Gender.MALE)
                .build();

        Account account = new Account();
        account.setUsername("testuser");
        account.setEmail("test@example.com");
        account.setPassword("encodedPassword");
        account.setRole(Role.STUDENT);
        account.setStatus(UserStatus.INACTIVE);

        // When
        when(modelMapper.map(registerReqDto, Account.class)).thenReturn(account);
        when(regex.isPhoneValid(anyString())).thenReturn(true);
        when(accountRepo.existsByUsername(anyString())).thenReturn(false);
        when(accountRepo.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(accountRepo.save(any(Account.class))).thenReturn(account);

        // Act
        Account newAccount = accountService.register(registerReqDto);

        // Then
        assertNotNull(newAccount);
        assertEquals(registerReqDto.getUsername(), newAccount.getUsername());
        assertEquals(registerReqDto.getEmail(), newAccount.getEmail());
        assertEquals(UserStatus.INACTIVE, newAccount.getStatus());
        assertEquals(Role.STUDENT, newAccount.getRole());

        verify(accountRepo, times(1)).save(any(Account.class));
        verify(modelMapper, times(1)).map(registerReqDto, Account.class);
        verify(passwordEncoder, times(1)).encode(registerReqDto.getPassword());
    }

    @Test
    void testRegister_UsernameExists() {
        // when
        when(regex.isPhoneValid(anyString())).thenReturn(true);
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
    void testRegister_EmailExists() {
        // when
        when(regex.isPhoneValid(anyString())).thenReturn(true);
        when(accountRepo.existsByEmail(anyString())).thenReturn(true);

        // given
        AppException exception = assertThrows(AppException.class, () -> {
            accountService.register(registerReqDto);
        });

        // then
        assertEquals(ErrorCode.EMAIL_EXISTS, exception.getErrorCode());
        verify(accountRepo, times(1)).existsByEmail(registerReqDto.getEmail());
        verify(accountRepo, never()).save(any(Account.class));
    }

    @Test
    void testRegister_PhoneNotValid() {
        // when
        when(regex.isPhoneValid(anyString())).thenReturn(false);

        // given
        AppException exception = assertThrows(AppException.class, () -> {
            accountService.register(registerReqDto);
        });

        // then
        assertEquals(ErrorCode.PHONE_NOT_VALID, exception.getErrorCode());
        verify(accountRepo, never()).save(any(Account.class));
    }

    @Test
    void testUploadAvatar_NullAvatar() {
        // When
        accountService.uploadAvatar(null, "folder", account);

        // Then
        assertEquals("defaultAvatar.jpg", account.getAvatar());
        verify(accountRepo, times(1)).save(account);
    }

    @Test
    void testUploadAvatar_ValidImage() throws IOException {
        // Given
        MockMultipartFile avatar = new MockMultipartFile("avatar", "avatar.jpg", "image/jpeg", new byte[]{1, 2, 3});
        when(fileUtil.isImage(any(MultipartFile.class))).thenReturn(true);
        when(fileService.linkSave(any(MultipartFile.class), anyString())).thenReturn("folder/avatar.jpg");

        // When
        accountService.uploadAvatar(avatar, "folder", account);

        // Then
        assertEquals("folder/avatar.jpg", account.getAvatar());
        verify(accountRepo, times(1)).save(account);
        verify(fileService, times(1)).linkSave(any(MultipartFile.class), anyString());
    }

    @Test
    void testUploadAvatar_InvalidImage() {
        // Given
        MockMultipartFile avatar = new MockMultipartFile("avatar", "avatar.txt", "text/plain", new byte[]{1, 2, 3});
        when(fileUtil.isImage(any(MultipartFile.class))).thenReturn(false);

        // When
        AppException exception = assertThrows(AppException.class, () -> {
            accountService.uploadAvatar(avatar, "folder", account);
        });

        // Then
        assertEquals(ErrorCode.FILE_INVALID_IMAGE, exception.getErrorCode());
        verify(accountRepo, never()).save(any(Account.class));
        verify(fileService, never()).linkSave(any(MultipartFile.class), anyString());
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
        // Given
        LoginReqDto loginReqDto1 = new LoginReqDto();
        loginReqDto1.setUsername("testuser");
        loginReqDto1.setPassword("password");

        Account account1 = new Account();
        account1.setUsername("testuser");
        account1.setPassword("password");
        account1.setStatus(UserStatus.INACTIVE);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(new UsernamePasswordAuthenticationToken(account1, "password"));

        // When
        AppException exception = assertThrows(AppException.class, () -> {
            accountService.login(loginReqDto1);
        });

        // Then
        assertEquals(ErrorCode.EMAIL_UNAUTHENTICATED, exception.getErrorCode());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(tokenHandler, never()).generateAccessToken(any(Account.class));
        verify(tokenHandler, never()).generateRefreshToken(any(Account.class));
        verifyNoMoreInteractions(authenticationManager, tokenHandler);
    }

    @Test
    void testAuthenticateAccount_Success() {
        // Mocking dependencies
        Otp otp1 = new Otp();
        otp1.setEmail("test@example.com");
        otp1.setOtpGeneratedTime(LocalDateTime.now().minusSeconds(otpExpiration - 500)); // Set a valid time
        otp1.setOtp("123456"); // Ensure OTP matches the expected value
        otp1.setValid(true);


        Account account1 = new Account();
        account1.setEmail("test@example.com");
        account1.setStatus(UserStatus.INACTIVE);

        // Mocking behavior
        when(otpService.findOtpByEmailAndValid(anyString(), anyBoolean())).thenReturn(otp1);
        when(accountRepo.findByEmail(anyString())).thenReturn(Optional.of(account1));
        when(accountRepo.save(any(Account.class))).thenReturn(account1);

        // Perform the test
        assertDoesNotThrow(() -> {
            accountService.authenticateAccount("test@example.com", "123456");
        });

        // Verify the account status
        assertEquals(UserStatus.ACTIVE, account1.getStatus());
        verify(accountRepo).save(account1);
    }

    @Test
    void testAuthenticateAccount_InvalidOtp() {
        // Mocking dependencies
        Otp otp1 = new Otp();
        otp1.setEmail("test@example.com");
        otp1.setOtpGeneratedTime(LocalDateTime.now().minusSeconds(otpExpiration - 500)); // Set a valid time
        otp1.setOtp("123456"); // Ensure OTP matches the expected value
        otp1.setValid(true);

        // Mocking behavior
        when(otpService.findOtpByEmailAndValid(anyString(), anyBoolean())).thenReturn(otp1);

        // Perform the test
        AppException exception = assertThrows(AppException.class, () -> {
            accountService.authenticateAccount("test@example.com", "654321");
        });

        // Verify the exception
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
    void testChangePassword_SameAsCurrentPassword() {
        // Given
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(passwordEncoder.matches(eq("password"), anyString())).thenReturn(true);
        when(passwordEncoder.matches(eq("newPassword"), anyString())).thenReturn(true); // New password matches current password

        ChangePasswordDto changePasswordDto = new ChangePasswordDto();
        changePasswordDto.setCurrentPassword("password");
        changePasswordDto.setNewPassword("newPassword");
        changePasswordDto.setConfirmNewPassword("newPassword");

        // When & Then
        AppException exception = assertThrows(AppException.class, () -> {
            accountService.changePassword(changePasswordDto);
        });

        assertEquals(ErrorCode.PASSWORD_IS_SAME_CURRENT, exception.getErrorCode());
    }

    @Test
    void testChangePassword_PasswordsDoNotMatch() {
        // Given
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(passwordEncoder.matches(eq("password"), anyString())).thenReturn(true);
        when(passwordEncoder.matches(eq("newPassword"), anyString())).thenReturn(false);

        ChangePasswordDto changePasswordDto = new ChangePasswordDto();
        changePasswordDto.setCurrentPassword("password");
        changePasswordDto.setNewPassword("newPassword");
        changePasswordDto.setConfirmNewPassword("differentPassword"); // New password and confirm password do not match

        // When & Then
        AppException exception = assertThrows(AppException.class, () -> {
            accountService.changePassword(changePasswordDto);
        });

        assertEquals(ErrorCode.PASSWORD_NOT_MATCH, exception.getErrorCode());
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

    private Account createAccount(String username, Role role) {
        Account account = new Account();
        account.setUsername(username);
        account.setRole(role);
        return account;
    }

    @Test
    void testGetListOfStudentAndInstructor_RoleStudent() {
        // Given
        Role role = Role.STUDENT;
        int offset = 1;
        int pageSize = 10;
        String sortBy = "username";
        List<Account> studentList = Arrays.asList(createAccount("student1", Role.STUDENT), createAccount("student2", Role.STUDENT));
        Page<Account> studentPage = new PageImpl<>(studentList);

        when(pageUtil.getPageable(sortBy, offset - 1, pageSize)).thenReturn(PageRequest.of(offset - 1, pageSize, Sort.by(sortBy)));
        when(accountRepo.findAccountByRole(eq(role), any(Pageable.class))).thenReturn(studentPage);

        // When
        Page<Account> result = accountService.getListOfStudentAndInstructor(role, offset, pageSize, sortBy);

        // Then
        assertEquals(2, result.getTotalElements());
        assertEquals("student1", result.getContent().get(0).getUsername());
        assertEquals("student2", result.getContent().get(1).getUsername());
        verify(accountRepo, times(1)).findAccountByRole(eq(role), any(Pageable.class));
    }

    @Test
    void testGetListOfStudentAndInstructor_RoleInstructor() {
        // Given
        Role role = Role.INSTRUCTOR;
        int offset = 1;
        int pageSize = 10;
        String sortBy = "username";
        List<Account> instructorList = Arrays.asList(createAccount("instructor1", Role.INSTRUCTOR), createAccount("instructor2", Role.INSTRUCTOR));
        Page<Account> instructorPage = new PageImpl<>(instructorList);

        when(pageUtil.getPageable(sortBy, offset - 1, pageSize)).thenReturn(PageRequest.of(offset - 1, pageSize, Sort.by(sortBy)));
        when(accountRepo.findAccountByRole(eq(role), any(Pageable.class))).thenReturn(instructorPage);

        // When
        Page<Account> result = accountService.getListOfStudentAndInstructor(role, offset, pageSize, sortBy);

        // Then
        assertEquals(2, result.getTotalElements());
        assertEquals("instructor1", result.getContent().get(0).getUsername());
        assertEquals("instructor2", result.getContent().get(1).getUsername());
        verify(accountRepo, times(1)).findAccountByRole(eq(role), any(Pageable.class));
    }

    @Test
    void testGetListOfStudentAndInstructor_RoleNull() {
        // Given
        Role role = null;
        int offset = 1;
        int pageSize = 10;
        String sortBy = "username";
        List<Account> instructorList = new ArrayList<>();
        instructorList.add(createAccount("instructor1", Role.INSTRUCTOR));
        instructorList.add(createAccount("instructor2", Role.INSTRUCTOR));

        List<Account> studentList = new ArrayList<>();
        studentList.add(createAccount("student1", Role.STUDENT));
        studentList.add(createAccount("student2", Role.STUDENT));

        List<Account> combinedList = new ArrayList<>(studentList);
        combinedList.addAll(instructorList);

        Page<Account> combinedPage = new PageImpl<>(combinedList);

        when(pageUtil.getPageable(sortBy, offset - 1, pageSize)).thenReturn(PageRequest.of(offset - 1, pageSize, Sort.by(sortBy)));
        when(accountRepo.findAccountByRole(Role.INSTRUCTOR)).thenReturn(instructorList);
        when(accountRepo.findAccountByRole(Role.STUDENT)).thenReturn(studentList);

        // When
        Page<Account> result = accountService.getListOfStudentAndInstructor(role, offset, pageSize, sortBy);

        // Then
        assertEquals(4, result.getTotalElements());
        assertEquals("student1", result.getContent().get(0).getUsername());
        assertEquals("student2", result.getContent().get(1).getUsername());
        assertEquals("instructor1", result.getContent().get(2).getUsername());
        assertEquals("instructor2", result.getContent().get(3).getUsername());
        verify(accountRepo, times(1)).findAccountByRole(Role.INSTRUCTOR);
        verify(accountRepo, times(1)).findAccountByRole(Role.STUDENT);
    }

    @Test
    void testGetListOfStudentAndInstructor_InvalidOffset() {
        // Given
        int invalidOffset = 0;
        int pageSize = 10;
        String sortBy = "username";

        doThrow(new AppException(ErrorCode.INVALID_OFFSET)).when(pageUtil).checkOffset(invalidOffset);

        // When & Then
        assertThrows(AppException.class, () -> accountService.getListOfStudentAndInstructor(Role.STUDENT, invalidOffset, pageSize, sortBy));
        verify(pageUtil, times(1)).checkOffset(invalidOffset);
    }

    @Test
    void testGetListOfStudentAndInstructor_NullSortBy() {
        // Given
        Role role = Role.STUDENT;
        int offset = 1;
        int pageSize = 10;
        String sortBy = null;
        List<Account> studentList = Arrays.asList(createAccount("student1", Role.STUDENT), createAccount("student2", Role.STUDENT));
        Page<Account> studentPage = new PageImpl<>(studentList);

        when(pageUtil.getPageable(sortBy, offset - 1, pageSize)).thenReturn(PageRequest.of(offset - 1, pageSize));
        when(accountRepo.findAccountByRole(eq(role), any(Pageable.class))).thenReturn(studentPage);

        // When
        Page<Account> result = accountService.getListOfStudentAndInstructor(role, offset, pageSize, sortBy);

        // Then
        assertEquals(2, result.getTotalElements());
        assertEquals("student1", result.getContent().get(0).getUsername());
        assertEquals("student2", result.getContent().get(1).getUsername());
        verify(accountRepo, times(1)).findAccountByRole(eq(role), any(Pageable.class));
    }

    private Account createAccount(String username, String avatar) {
        Account account = new Account();
        account.setUsername(username);
        account.setAvatar(avatar);
        return account;
    }

    @Test
    void testGetProfile_WithAvatar() {
        // Given
        String avatarFilename = "avatar.jpg";
        String signedUrl = "http://signed-url";
        Account account = createAccount("user", avatarFilename);

        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(fileService.getSignedImageUrl(avatarFilename)).thenReturn(signedUrl);

        // When
        Account result = accountService.getProfile();

        // Then
        assertNotNull(result);
        assertEquals(account.getUsername(), result.getUsername());
        assertEquals(signedUrl, result.getAvatar());
        verify(accountUtil, times(1)).getCurrentAccount();
        verify(fileService, times(1)).getSignedImageUrl(avatarFilename);
    }

    @Test
    void testGetProfile_WithoutAvatar() {
        // Given
        Account account = createAccount("user", (String) null);

        when(accountUtil.getCurrentAccount()).thenReturn(account);

        // When
        Account result = accountService.getProfile();

        // Then
        assertNotNull(result);
        assertEquals(account.getUsername(), result.getUsername());
        assertNull(result.getAvatar());
        verify(accountUtil, times(1)).getCurrentAccount();
        verify(fileService, never()).getSignedImageUrl(anyString()); // fileService shouldn't be called
    }

    private Account createAccount1(String email, String avatar) {
        Account account = new Account();
        account.setEmail(email);
        account.setAvatar(avatar);
        return account;
    }

    @Test
    void testGetAccountByEmail_WithAvatar() {
        // Given
        String email = "test@example.com";
        String avatarFilename = "avatar.jpg";
        String signedUrl = "http://signed-url";
        Account account = createAccount1(email, avatarFilename);

        when(accountRepo.findByEmail(email)).thenReturn(Optional.of(account));
        when(fileService.getSignedImageUrl(avatarFilename)).thenReturn(signedUrl);

        // When
        Account result = accountService.getAccountByEmail(email);

        // Then
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertEquals(signedUrl, result.getAvatar());
        verify(accountRepo, times(1)).findByEmail(email);
        verify(fileService, times(1)).getSignedImageUrl(avatarFilename);
    }

    @Test
    void testGetAccountByEmail_WithoutAvatar() {
        // Given
        String email = "test@example.com";
        Account account = createAccount1(email, (String) null);

        when(accountRepo.findByEmail(email)).thenReturn(Optional.of(account));

        // When
        Account result = accountService.getAccountByEmail(email);

        // Then
        assertNotNull(result);
        assertEquals(email, result.getEmail());
        assertNull(result.getAvatar());
        verify(accountRepo, times(1)).findByEmail(email);
        verify(fileService, never()).getSignedImageUrl(anyString()); // fileService shouldn't be called
    }

    @Test
    void testGetAccountByEmail_EmailNotFound() {
        // Given
        String email = "test@example.com";

        when(accountRepo.findByEmail(email)).thenReturn(Optional.empty());

        // When / Then
        AppException exception = assertThrows(AppException.class, () -> accountService.getAccountByEmail(email));
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());
        verify(accountRepo, times(1)).findByEmail(email);
        verify(fileService, never()).getSignedImageUrl(anyString()); // fileService shouldn't be called
    }

    @Test
    void testResetPassword_Success() {
        // Given
        String email = "test@example.com";
        String otp = "123456";
        ResetPasswordDto resetPasswordDto = new ResetPasswordDto();
        resetPasswordDto.setPassword("newpassword");
        resetPasswordDto.setConfirmPassword("newpassword");

        Account account = new Account();
        account.setEmail(email);
        account.setPassword("oldpassword");

        Otp userOtp = new Otp();
        userOtp.setEmail(email);
        userOtp.setOtp(otp);
        userOtp.setOtpGeneratedTime(LocalDateTime.now().minusSeconds(otpExpiration - 500)); // Valid OTP time

        when(accountRepo.findByEmail(email)).thenReturn(Optional.of(account));
        when(otpService.findOtpByEmailAndValid(email, true)).thenReturn(userOtp);
        when(passwordEncoder.encode(resetPasswordDto.getPassword())).thenReturn("encodedPassword");

        // When
        accountService.resetPassword(email, otp, resetPasswordDto);

        // Then
        verify(accountRepo, times(1)).save(account);
        verify(otpService, times(1)).updateOldOtps(email);
        assertEquals("encodedPassword", account.getPassword());
    }

    @Test
    void testResetPassword_InvalidOtp() {
        // Given
        String email = "test@example.com";
        String otp = "123456";
        ResetPasswordDto resetPasswordDto = new ResetPasswordDto();
        resetPasswordDto.setPassword("newpassword");
        resetPasswordDto.setConfirmPassword("newpassword");

        Account account = new Account();
        account.setEmail(email);
        account.setPassword("oldpassword");

        Otp userOtp = new Otp();
        userOtp.setEmail(email);
        userOtp.setOtp("654321"); // Different OTP
        userOtp.setOtpGeneratedTime(LocalDateTime.now().minusSeconds(otpExpiration - 500)); // Valid OTP time

        when(accountRepo.findByEmail(email)).thenReturn(Optional.of(account));
        when(otpService.findOtpByEmailAndValid(email, true)).thenReturn(userOtp);

        // When
        AppException exception = assertThrows(AppException.class, () -> {
            accountService.resetPassword(email, otp, resetPasswordDto);
        });

        // Then
        assertEquals(ErrorCode.OTP_INVALID, exception.getErrorCode());
        verify(accountRepo, never()).save(any(Account.class));
    }

    @Test
    void testResetPassword_ExpiredOtp() {
        // Given
        String email = "test@example.com";
        String otp = "123456";
        ResetPasswordDto resetPasswordDto = new ResetPasswordDto();
        resetPasswordDto.setPassword("newpassword");
        resetPasswordDto.setConfirmPassword("newpassword");

        Account account = new Account();
        account.setEmail(email);
        account.setPassword("oldpassword");

        Otp userOtp = new Otp();
        userOtp.setEmail(email);
        userOtp.setOtp(otp);
        userOtp.setOtpGeneratedTime(LocalDateTime.now().minusSeconds(otpExpiration + 1)); // Expired OTP time

        when(accountRepo.findByEmail(email)).thenReturn(Optional.of(account));
        when(otpService.findOtpByEmailAndValid(email, true)).thenReturn(userOtp);

        // When
        AppException exception = assertThrows(AppException.class, () -> {
            accountService.resetPassword(email, otp, resetPasswordDto);
        });

        // Then
        assertEquals(ErrorCode.OTP_EXPIRED, exception.getErrorCode());
        verify(accountRepo, never()).save(any(Account.class));
        verify(otpService, times(1)).saveOtp(userOtp.getEmail(), userOtp.getOtp());
        assertFalse(userOtp.getValid());
    }


    @Test
    void testSaveAccount() {
        //when
        Account account1 = new Account();
        account1.setUsername("testUser");

        //given
        accountService.saveAccount(account1);

        //then
        verify(accountRepo, times(1)).save(account1);
    }

    @Test
    void testGetAccountByUsername_Found() {
        //when
        String username = "testUser";
        Account account1 = new Account();
        account1.setUsername(username);
        when(accountRepo.findByUsername(username)).thenReturn(Optional.of(account1));

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
    void testSetAdmin_UserExists() {
        String username = "testUser";
        Account currentUser = new Account();
        currentUser.setUsername("currentAdmin");

        when(accountRepo.findByUsername(username)).thenReturn(Optional.of(account));
        when(accountUtil.getCurrentAccount()).thenReturn(currentUser);

        accountService.setAdmin(username);

        assertEquals(Role.ADMIN, account.getRole());
        assertEquals(UserStatus.ACTIVE, account.getStatus());
        assertEquals("currentAdmin", account.getUpdatedBy());

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
    void testApproveInstructorById_UserExists_Approved() {
        //when
        Long userId = 1L;
        account.setRole(Role.STUDENT);
        account.setInstructorStatus(InstructorStatus.WAITING);
        when(accountRepo.findById(userId)).thenReturn(Optional.of(account));

        //given
        accountService.approveInstructorById(userId, InstructorStatus.APPROVED);
        assertEquals(Role.INSTRUCTOR, account.getRole());
        assertEquals(InstructorStatus.APPROVED, account.getInstructorStatus());

        //then
        verify(accountRepo, times(1)).save(account);
    }

    @Test
    void testApproveInstructorById_UserExists_Rejected() {
        //when
        Long userId = 1L;
        account.setInstructorStatus(InstructorStatus.WAITING);
        when(accountRepo.findById(userId)).thenReturn(Optional.of(account));

        //given
        accountService.approveInstructorById(userId, InstructorStatus.REJECTED);
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
            accountService.approveInstructorById(userId, InstructorStatus.APPROVED);
        });
        assertEquals(ErrorCode.USER_NOT_FOUND, exception.getErrorCode());

        //then
        verify(accountRepo, never()).save(any(Account.class));
    }

//    @Test
//    void testSendCv() throws IOException {
//        // Given
//        MockMultipartFile mockFile = new MockMultipartFile("cv", "cv.pdf", "application/pdf", "CV content".getBytes());
//        Account account = new Account();
//        account.setCvLink(null);
//        account.setInstructorStatus(null);
//
//        when(accountUtil.getCurrentAccount()).thenReturn(account);
//        when(fileUtil.isPDF(mockFile)).thenReturn(true);
//
//        // Mock the uploadFile method to do nothing
//        doNothing().when(fileService).uploadFile(any(MultipartFile.class));
//        when(accountRepo.save(any(Account.class))).thenReturn(account);
//
//        // When
//        accountService.sendCv(mockFile);
//
//        // Then
//        assertEquals(InstructorStatus.WAITING, account.getInstructorStatus()); // Ensure the instructor status is updated
//        verify(accountRepo, times(1)).save(account); // Verify that the account is saved
//        verify(fileService, times(1)).uploadFile(mockFile); // Verify that uploadFile is called once
//    }

    @Test
    void testSendCv_InvalidFile() {
        MockMultipartFile mockFile = new MockMultipartFile("cv", "cv.txt", "text/plain", "CV content".getBytes());

        when(accountUtil.getCurrentAccount()).thenReturn(account);
        when(fileUtil.isPDF(mockFile)).thenReturn(false);

        AppException exception = assertThrows(AppException.class, () -> accountService.sendCv(mockFile));
        assertEquals(ErrorCode.FILE_INVALID_PDF, exception.getErrorCode());

        verify(accountRepo, never()).save(account);
    }

//    @Test
//    void testSendCv_FileUploadFail() throws IOException {
//        // Given
//        MockMultipartFile mockFile = new MockMultipartFile("cv", "cv.pdf", "application/pdf", "CV content".getBytes());
//        Account account = new Account();
//        account.setCvLink(null);
//        account.setInstructorStatus(null);
//
//        when(accountUtil.getCurrentAccount()).thenReturn(account);
//        when(fileUtil.isPDF(mockFile)).thenReturn(true);
//
//        // Mock the uploadFile method to throw IOException
//        doThrow(new IOException("Upload failed")).when(fileService).uploadFile(any(MultipartFile.class));
//
//        // When
//        AppException exception = assertThrows(AppException.class, () -> accountService.sendCv(mockFile));
//
//        // Then
//        assertEquals(ErrorCode.FILE_UPLOAD_FAIL, exception.getErrorCode());
//        verify(accountRepo, never()).save(account); // Verify that save is never called
//        verify(fileService, times(1)).uploadFile(mockFile); // Verify that uploadFile is called once
//    }

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
    void testGetInstructorByName_Found() {
        // given
        String name = "John Doe";
        Account instructor = new Account();
        instructor.setFullName("John Doe");
        instructor.setInstructorStatus(InstructorStatus.APPROVED);

        when(accountRepo.findByFullNameLikeAndInstructorStatus("%" + name + "%", InstructorStatus.APPROVED))
                .thenReturn(List.of(instructor));

        // when
        List<Account> result = accountService.getInstructorByName(name);

        // then
        assertFalse(result.isEmpty());
        assertEquals("John Doe", result.get(0).getFullName());
        assertEquals(InstructorStatus.APPROVED, result.get(0).getInstructorStatus());
    }

    @Test
    void testGetInstructorByName_NotFound() {
        // given
        String name = "Nonexistent";
        when(accountRepo.findByFullNameLikeAndInstructorStatus("%" + name + "%", InstructorStatus.APPROVED))
                .thenReturn(List.of());

        // when + then
        AppException exception = assertThrows(AppException.class, () -> {
            accountService.getInstructorByName(name);
        });

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
    void testSetStatusAccount_Success() {
        // Given
        String username = "testuser";
        UserStatus newStatus = UserStatus.ACTIVE;
        Account existingAccount = new Account();
        existingAccount.setUsername(username);
        existingAccount.setStatus(UserStatus.INACTIVE);
        Account updatedAccount = new Account();
        updatedAccount.setUsername(username);
        updatedAccount.setStatus(newStatus);

        when(accountRepo.findByUsername(username)).thenReturn(Optional.of(existingAccount));
        when(accountRepo.save(any(Account.class))).thenReturn(updatedAccount);

        // When
        Account result = accountService.setStatusAccount(username, newStatus);

        // Then
        assertEquals(newStatus, result.getStatus());
        verify(accountRepo, times(1)).findByUsername(username);
        verify(accountRepo, times(1)).save(any(Account.class));
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
    void testSubscribeInstructor_JsonProcessingException() throws Exception {
        // Given
        instructorAccount.setId(1L);
        account.setId(2L);
        List<Long> subscribing = List.of(3L, 4L); // của user
        String invalidJsonForSubscriber = "invalid";
        String subscribingJson = objectMapper.writeValueAsString(subscribing);
        account.setSubscribingJson(subscribingJson);
        instructorAccount.setSubscribersJson(invalidJsonForSubscriber);

        // When
        when(accountRepo.findById(instructorAccount.getId())).thenReturn(Optional.of(instructorAccount));
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        doThrow(new JsonProcessingException("Invalid JSON") {
        })
                .when(objectMapper).readValue(eq(invalidJsonForSubscriber), any(TypeReference.class));

        // Then
        AppException exception = assertThrows(AppException.class, () -> {
            accountService.subscribeInstructor(instructorAccount.getId());
        });
        assertEquals(ErrorCode.PROCESS_ADD_STUDIED_COURSE_FAIL, exception.getErrorCode());
    }

    @Test
    void testUnsubscribeInstructor_JsonProcessingException() throws Exception {
        // Given
        instructorAccount.setId(1L);
        account.setId(2L);
        String invalidJsonForSubscribing = "invalid";
        List<Long> subscriber = List.of(5L, 6L);
        String subscriberJson = objectMapper.writeValueAsString(subscriber);
        account.setSubscribingJson(invalidJsonForSubscribing);
        instructorAccount.setSubscribersJson(subscriberJson);

        // When
        when(accountRepo.findById(instructorAccount.getId())).thenReturn(Optional.of(instructorAccount));
        when(accountUtil.getCurrentAccount()).thenReturn(account);
        doThrow(new JsonProcessingException("Invalid JSON") {
        })
                .when(objectMapper).readValue(eq(invalidJsonForSubscribing), any(TypeReference.class));

        // Then
        AppException exception = assertThrows(AppException.class, () -> {
            accountService.unsubscribeInstructor(instructorAccount.getId());
        });
        assertEquals(ErrorCode.PROCESS_ADD_STUDIED_COURSE_FAIL, exception.getErrorCode());
    }


    @Test
    void testGetSubscribingsAndSubcriberUsers_BeEmpty() throws Exception {

        //given
        instructorAccount.setId(1L);
        account.setId(2L);

        String emptysubcriberJson = "";
        String emptysubcribingJson = "";
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

