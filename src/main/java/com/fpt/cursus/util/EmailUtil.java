package com.fpt.cursus.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class EmailUtil {
    @Autowired
    private JavaMailSender javaMailSender;

    public void sendOtpEmail(String email, String otp) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        mimeMessageHelper.setTo(email);
        mimeMessageHelper.setSubject("Xác nhận địa chỉ email của bạn");
        mimeMessageHelper.setText("""
        <div>
          Gửi %s,<br>
          Để xác thực địa chỉ email đã đăng ký vui lòng ấn
          <a href="http://localhost:8080/verifyAccount?email=%s&otp=%s" target="_blank"> vào đây</a>
        </div>
        """.formatted(email, email, otp), true);

        javaMailSender.send(mimeMessage);
    }

}
