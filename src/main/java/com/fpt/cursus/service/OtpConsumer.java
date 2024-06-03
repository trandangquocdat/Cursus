package com.fpt.cursus.service;
import com.fpt.cursus.entity.Otp;
import com.fpt.cursus.exception.exceptions.AppException;
import com.fpt.cursus.exception.exceptions.ErrorCode;
import com.fpt.cursus.repository.OtpRepo;
import com.fpt.cursus.util.EmailUtil;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OtpConsumer {
    @Autowired
    private OtpRepo otpRepo;

    @Autowired
    private EmailUtil emailUtil;

    @KafkaListener(topics = "service-topic", groupId = "service")
    public void processEmail(String message) {
        String[] parts = message.split(",");
        String email = parts[0];
        String otp = parts[1];
        try {
            emailUtil.sendOtpEmail(email, otp);
            Otp accountOtp = new Otp();
            accountOtp.setEmail(email);
            accountOtp.setOtp(otp);
            accountOtp.setOtpGeneratedTime(LocalDateTime.now());
            otpRepo.save(accountOtp);
        } catch (MessagingException e) {
            throw new AppException(ErrorCode.EMAIL_CAN_NOT_SEND);
        }
    }
}
