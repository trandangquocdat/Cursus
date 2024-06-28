package com.fpt.cursus.service;

import com.fpt.cursus.entity.Otp;

public interface OtpService {
    String generateOtp();

    void updateOldOtps(String email);

    Otp findOtpByEmailAndValid(String email, Boolean valid);

    void saveOtp(String email, String otp);

    void sendOtpEmail(String email, String otp);

    void sendResetPasswordEmail(String email, String otp);

    void deleteOldOtps();
}
