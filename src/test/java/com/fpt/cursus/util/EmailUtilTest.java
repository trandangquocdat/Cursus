package com.fpt.cursus.util;

import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.test.context.ContextConfiguration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ContextConfiguration(classes = EmailUtil.class)
class EmailUtilTest {

    @InjectMocks
    private EmailUtil emailUtil;

    @Mock
    private JavaMailSender javaMailSender;

    private String email;

    @BeforeEach
    void setUp() {
        email = "test@test.com";
    }

    @Test
    void testSendEmail() throws MessagingException {
        //when
        when(javaMailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        doNothing().when(javaMailSender).send(any(MimeMessage.class));
        //then
        emailUtil.sendEmail(email, "Test", "Test");
        verify(javaMailSender, times(1))
                .send(any(MimeMessage.class));
    }

    @Test
    void sendOtpEmail() throws MessagingException {
        //when
        when(javaMailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        doNothing().when(javaMailSender).send(any(MimeMessage.class));
        //then
        emailUtil.sendOtpEmail(email, "123456");
        verify(javaMailSender, times(1))
                .send(any(MimeMessage.class));
    }

    @Test
    void sendPasswordResetEmail() throws MessagingException {
        //when
        when(javaMailSender.createMimeMessage()).thenReturn(new MimeMessage((Session) null));
        doNothing().when(javaMailSender).send(any(MimeMessage.class));
        //then
        emailUtil.sendPasswordResetEmail(email, "123456");
        verify(javaMailSender, times(1))
                .send(any(MimeMessage.class));
    }

}
