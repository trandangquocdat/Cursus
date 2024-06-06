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

    public void sendEmail(String email, String subject, String content) throws MessagingException {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
        mimeMessageHelper.setTo(email);
        mimeMessageHelper.setSubject(subject);
        mimeMessageHelper.setText(content, true);
        javaMailSender.send(mimeMessage);
    }

    public void sendOtpEmail(String email, String otp) throws MessagingException {
        String subject = "[Cursus] Email verification";
        String content = """
                <div>
                  Dear %s,<br>
                  If you want to verify your email, please
                  <a href="http://localhost:8080/verify-account?email=%s&otp=%s" target="_blank"> Click here</a>
                  <br><br>
                              <div style="border-top:1px solid #eaeaea; padding-top:10px;">
                                Best Regards,<br>
                                Cursus team<br>
                              </div>
                </div>
                """.formatted(email, email, otp);

        sendEmail(email, subject, content);
    }

    public void sendPasswordResetEmail(String email, String otp) throws MessagingException {
        String subject = "Reset Password";
        String content = """
                <div>
                  Dear %s,<br>
                  If you want to reset password, please
                  <a href="http://localhost:8080/verify-account?email=%s&otp=%s" target="_blank"> Click here</a>
                    <br><br>
                              <div style="border-top:1px solid #eaeaea; padding-top:10px;">
                                Best Regards,<br>
                                Cursus team<br>
                              </div>
                </div>
                """.formatted(email, email, otp);

        sendEmail(email, subject, content);
    }
}
