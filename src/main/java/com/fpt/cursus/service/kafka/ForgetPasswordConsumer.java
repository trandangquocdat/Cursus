package com.fpt.cursus.service.kafka;

import com.fpt.cursus.entity.Otp;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.repository.OtpRepo;
import com.fpt.cursus.util.EmailUtil;
import jakarta.mail.MessagingException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ForgetPasswordConsumer {
    private OtpRepo otpRepo;
    private EmailUtil emailUtil;

    public ForgetPasswordConsumer(OtpRepo otpRepo, EmailUtil emailUtil) {
        this.otpRepo = otpRepo;
        this.emailUtil = emailUtil;
    }

    @KafkaListener(topics = "service-topic-fp", groupId = "service3")
    public void processEmail1(String message) {
        String[] parts = message.split(",");
        if (parts.length == 2) {
            String email = parts[0];
            String otp = parts[1];
            try {
                emailUtil.sendPasswordResetEmail(email, otp);
                Otp accountOtp = new Otp();
                accountOtp.setEmail(email);
                accountOtp.setOtp(otp);
                accountOtp.setOtpGeneratedTime(LocalDateTime.now());
                otpRepo.save(accountOtp);
            } catch (MessagingException e) {
                throw new AppException(ErrorCode.EMAIL_CAN_NOT_SEND);
            }
        } else {
            throw new IllegalArgumentException("Invalid message format");
        }
    }

}
