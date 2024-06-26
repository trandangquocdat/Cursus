package com.fpt.cursus.service;

public interface OtpService {

    void deleteOldOtps();

    void sendResetPasswordEmail(String email, String otp);

    void sendOtpEmail(String email, String otp);

    void saveOtp(String email, String otp);

    String generateOtp();
}
