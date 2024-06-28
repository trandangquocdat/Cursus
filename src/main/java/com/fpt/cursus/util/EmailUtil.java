package com.fpt.cursus.util;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

@Component
public class EmailUtil {

    @Autowired
    private JavaMailSender javaMailSender;

    @Value("${spring.host}")
    private String host;
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
        String url = "http://" + host + ":8080/auth/verify-account?email=" + email + "&otp=" + otp;
        String content = """
            <div>
              Dear %s,<br>
              If you want to verify your email, please
              <a href="%s" target="_blank">Click here</a>
              <br><br>
                          <div style="border-top:1px solid #eaeaea; padding-top:10px;">
                            Best Regards,<br>
                            Cursus team<br>
                          </div>
            </div>
            """.formatted(email, url);

        sendEmail(email, subject, content);
    }

    public void sendPasswordResetEmail(String email, String otp) throws MessagingException {
        String subject = "[Cursus] Reset Password";
        String url = "http://" + host + ":8080/auth/reset-password?email=" + email + "&otp=" + otp;
        String content = """
                <div>
                  Dear %s,<br>
                  If you want to reset password, please
                  <a href="%s" target="_blank">Click here</a>
                    <br><br>
                              <div style="border-top:1px solid #eaeaea; padding-top:10px;">
                                Best Regards,<br>
                                Cursus team<br>
                              </div>
                </div>
                """.formatted(email, url);

        sendEmail(email, subject, content);
    }
}
