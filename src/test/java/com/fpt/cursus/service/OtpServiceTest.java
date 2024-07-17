//package com.fpt.cursus.service;
//
//import com.fpt.cursus.entity.Otp;
//import com.fpt.cursus.exception.exceptions.AppException;
//import com.fpt.cursus.exception.exceptions.ErrorCode;
//import com.fpt.cursus.repository.OtpRepo;
//import com.fpt.cursus.service.impl.OtpServiceImpl;
//import com.fpt.cursus.util.EmailUtil;
//import com.fpt.cursus.util.OtpUtil;
//import jakarta.mail.MessagingException;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//
//import java.time.LocalDateTime;
//
//import static org.junit.jupiter.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//class OtpServiceTest {
//
//    @InjectMocks
//    OtpServiceImpl otpService;
//
//    @Mock
//    OtpRepo otpRepo;
//
//    @Mock
//    OtpUtil otpUtil;
//
//    @Mock
//    EmailUtil emailUtil;
//
//    private Otp otp;
//
//    @BeforeEach
//    void setUp() {
//        otp = new Otp();
//        otp.setEmail("test@example.com");
//        otp.setOtp("123456");
//        otp.setOtpGeneratedTime(LocalDateTime.now());
//        otp.setValid(true);
//    }
//
//    @Test
//    void testGenerateOtp() {
//        //when
//        when(otpUtil.generateOtp()).thenReturn("123456");
//
//        //given
//        String generatedOtp = otpService.generateOtp();
//
//        //then
//        assertEquals("123456", generatedOtp);
//    }
//
//    @Test
//    void testSaveOtp() {
//        //when
//        when(otpRepo.save(any(Otp.class))).thenReturn(otp);
//
//        //given
//        otpService.saveOtp("test@example.com", "123456");
//
//        //then
//        verify(otpRepo, times(1)).save(any(Otp.class));
//    }
//
//    @Test
//    void testSendOtpEmail_Success() throws MessagingException {
//        //when
//        doNothing().when(emailUtil).sendOtpEmail(anyString(), anyString());
//
//        //given
//        assertDoesNotThrow(() -> otpService.sendOtpEmail("test@example.com", "123456"));
//
//        //then
//        verify(emailUtil, times(1)).sendOtpEmail(anyString(), anyString());
//    }
//
//    @Test
//    void testSendOtpEmail_Failure() throws MessagingException {
//        //when
//        doThrow(MessagingException.class).when(emailUtil).sendOtpEmail(anyString(), anyString());
//
//        //given
//        AppException exception = assertThrows(AppException.class, () -> otpService.sendOtpEmail("test@example.com", "123456"));
//
//        //then
//        assertEquals(ErrorCode.EMAIL_CAN_NOT_SEND, exception.getErrorCode());
//    }
//
//    @Test
//    void testSendResetPasswordEmail_Success() throws MessagingException {
//        //when
//        doNothing().when(emailUtil).sendPasswordResetEmail(anyString(), anyString());
//
//        //given
//        assertDoesNotThrow(() -> otpService.sendResetPasswordEmail("test@example.com", "123456"));
//
//        //then
//        verify(emailUtil, times(1)).sendPasswordResetEmail(anyString(), anyString());
//    }
//
//    @Test
//    void testSendResetPasswordEmail_Failure() throws MessagingException {
//        //when
//        doThrow(MessagingException.class).when(emailUtil).sendPasswordResetEmail(anyString(), anyString());
//
//        //given
//        AppException exception = assertThrows(AppException.class, () -> otpService.sendResetPasswordEmail("test@example.com", "123456"));
//
//        //then
//        assertEquals(ErrorCode.EMAIL_CAN_NOT_SEND, exception.getErrorCode());
//    }
//
//    @Test
//    void testDeleteOldOtps() {
//        //when
//        doNothing().when(otpRepo).deleteInvalidOrExpiredOtps(any(LocalDateTime.class));
//
//        //given
//        otpService.deleteOldOtps();
//
//        //then
//        verify(otpRepo, times(1)).deleteInvalidOrExpiredOtps(any(LocalDateTime.class));
//    }
//
//    @Test
//    void testUpdateOldOtps() {
//        //when
//        doNothing().when(otpRepo).updateOldOtps(anyString());
//
//        //given
//        otpService.updateOldOtps("test@example.com");
//
//        //then
//        verify(otpRepo, times(1)).updateOldOtps(anyString());
//    }
//
//    @Test
//    void testFindOtpByEmailAndValid_Success() {
//        //when
//        when(otpRepo.findOtpByEmailAndValid(anyString(), anyBoolean())).thenReturn(otp);
//
//        //given
//        Otp foundOtp = otpService.findOtpByEmailAndValid("test@example.com", true);
//
//        //then
//        assertNotNull(foundOtp);
//        assertEquals(otp.getEmail(), foundOtp.getEmail());
//        assertEquals(otp.getOtp(), foundOtp.getOtp());
//    }
//
//    @Test
//    void testFindOtpByEmailAndValid_Failure() {
//        //when
//        when(otpRepo.findOtpByEmailAndValid(anyString(), anyBoolean())).thenReturn(null);
//
//        //given
//        AppException exception = assertThrows(AppException.class, () -> otpService.findOtpByEmailAndValid("test@example.com", true));
//
//        //then
//        assertEquals(ErrorCode.OTP_NOT_FOUND, exception.getErrorCode());
//    }
//}
