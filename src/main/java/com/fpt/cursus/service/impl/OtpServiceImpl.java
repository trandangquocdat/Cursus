package com.fpt.cursus.service.impl;

import com.fpt.cursus.entity.Otp;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.repository.OtpRepo;
import com.fpt.cursus.service.OtpService;
import com.fpt.cursus.util.EmailUtil;
import com.fpt.cursus.util.OtpUtil;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OtpServiceImpl implements OtpService {
    @Autowired
    private OtpRepo otpRepo;

    @Autowired
    private OtpUtil otpUtil;

    @Autowired
    private EmailUtil emailUtil;

    public String generateOtp() {
        return otpUtil.generateOtp();
    }

    @Async
    public void saveOtp(String email, String otp) {
        Otp otpEntity = new Otp();
        otpEntity.setEmail(email);
        otpEntity.setOtp(otp);
        otpEntity.setOtpGeneratedTime(LocalDateTime.now());
        otpRepo.save(otpEntity);
    }

    @Async
    public void sendOtpEmail(String email, String otp) {
        try {
            emailUtil.sendOtpEmail(email, otp);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.EMAIL_CAN_NOT_SEND);
        }
    }

    @Async
    public void sendResetPasswordEmail(String email, String otp) {
        try {
            emailUtil.sendPasswordResetEmail(email, otp);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.EMAIL_CAN_NOT_SEND);
        }
    }

    @Scheduled(cron = "0 0 0 * * ?") // Lập lịch chạy mỗi ngày vào nửa đêm
    public void deleteOldOtps() {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        otpRepo.deleteInvalidOrExpiredOtps(thirtyDaysAgo);
    }
}
