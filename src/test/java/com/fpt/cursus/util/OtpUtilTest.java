package com.fpt.cursus.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ContextConfiguration;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = OtpUtil.class)
class OtpUtilTest {

    @InjectMocks
    private OtpUtil otpUtil;

    @Test
    void generateOtp() {
        //then
        String otp = otpUtil.generateOtp();
        assertEquals(6, otp.length());
        assertTrue(Pattern.matches("\\d{6}", otp));
    }

}
