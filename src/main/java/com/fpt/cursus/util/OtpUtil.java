package com.fpt.cursus.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class OtpUtil {
    private static final Random random = new Random();

    public String generateOtp() {
        int randomNumber = random.nextInt(999999);
        StringBuilder output = new StringBuilder(Integer.toString(randomNumber));
        while (output.length() < 6) {
            output.insert(0, "0");
        }
        return output.toString();
    }


}
