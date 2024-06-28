package com.fpt.cursus.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Random;

@Component
public class OtpUtil {
    @Autowired
    EmailUtil emailUtil;
    public String generateOtp() {
        Random random = new Random();
        int randomNumber = random.nextInt(999999);
        StringBuilder output = new StringBuilder(Integer.toString(randomNumber));
        while (output.length() < 6) {
            output.insert(0, "0");
        }
        return output.toString();
    }


}
