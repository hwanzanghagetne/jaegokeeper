package com.jaegokeeper.mail;

import com.jaegokeeper.common.mail.GmailMailService;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class MailServiceTest {

    private JavaMailSender mailSender;
    private SpringTemplateEngine mailTemplateEngine;
    private GmailMailService gmailMailService;

    @Before
    public void setUp() {
        mailSender = mock(JavaMailSender.class);
        mailTemplateEngine = mock(SpringTemplateEngine.class);
        gmailMailService = new GmailMailService(mailSender, mailTemplateEngine);
    }

    @Test
    public void 회원가입_인증메일_전송_성공() throws Exception {
        MimeMessage message = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(message);
        when(mailTemplateEngine.process(eq("signup"), any())).thenReturn("<html>123456</html>");

        gmailMailService.sendSignupCode("test@example.com", "123456");

        verify(mailSender).send(message);
    }

    @Test
    public void 환영메일_전송_성공() throws Exception {
        MimeMessage message = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(message);
        when(mailTemplateEngine.process(eq("welcome"), any())).thenReturn("<html>홍길동</html>");

        gmailMailService.sendWelcome("test@example.com", "홍길동");

        verify(mailSender).send(message);
    }
}
