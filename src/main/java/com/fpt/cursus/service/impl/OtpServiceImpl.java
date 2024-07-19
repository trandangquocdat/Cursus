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
    private final OtpRepo otpRepo;

    private final OtpUtil otpUtil;

    private final EmailUtil emailUtil;

    @Autowired
    public OtpServiceImpl(OtpRepo otpRepo,
                          OtpUtil otpUtil,
                          EmailUtil emailUtil) {
        this.otpRepo = otpRepo;
        this.otpUtil = otpUtil;
        this.emailUtil = emailUtil;
    }

    @Override
    public String generateOtp() {
        return otpUtil.generateOtp();
    }

    @Override
    public void updateOldOtps(String email) {
        otpRepo.updateOldOtps(email);
    }

    @Override
    public Otp findOtpByEmailAndValid(String email, Boolean valid) {
        Otp otp = otpRepo.findOtpByEmailAndValid(email, valid);
        if (otp == null) {
            throw new AppException(ErrorCode.OTP_NOT_FOUND);
        }
        return otpRepo.findOtpByEmailAndValid(email, valid);
    }

    @Override
    @Async
    public void saveOtp(String email, String otp) {
        Otp otpEntity = new Otp();
        otpEntity.setEmail(email);
        otpEntity.setOtp(otp);
        otpEntity.setOtpGeneratedTime(LocalDateTime.now());
        otpRepo.save(otpEntity);
    }

    @Override
    @Async
    public void sendOtpEmail(String email, String otp) {
        try {
            emailUtil.sendOtpEmail(email, otp);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.EMAIL_CAN_NOT_SEND);
        }
    }

    @Override
    @Async
    public void sendResetPasswordEmail(String email, String otp) {
        try {
            emailUtil.sendPasswordResetEmail(email, otp);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.EMAIL_CAN_NOT_SEND);
        }
    }

    @Override
    @Scheduled(cron = "0 0 0 * * ?") // Lập lịch chạy mỗi ngày vào nửa đêm
    public void deleteOldOtps() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        otpRepo.deleteInvalidOrExpiredOtps(sevenDaysAgo);
    }

}
