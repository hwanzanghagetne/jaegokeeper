package com.jaegokeeper.mail;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
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

    @Test
    public void 첫_전송이_실패해도_한번_더_시도해서_성공하면_종료() throws Exception {
        MimeMessage message = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(message);
        when(mailTemplateEngine.process(eq("signup"), any())).thenReturn("<html>123456</html>");
        doThrow(new MailSendException("smtp down")).doNothing().when(mailSender).send(message);

        gmailMailService.sendSignupCode("test@example.com", "123456");

        verify(mailSender, times(2)).send(message);
    }

    @Test
    public void 두번_모두_실패해도_예외를_던지지_않고_두번만_시도() throws Exception {
        MimeMessage message = new MimeMessage(Session.getInstance(new Properties()));
        when(mailSender.createMimeMessage()).thenReturn(message);
        when(mailTemplateEngine.process(eq("welcome"), any())).thenReturn("<html>홍길동</html>");
        doThrow(new MailSendException("smtp down")).when(mailSender).send(message);

        gmailMailService.sendWelcome("test@example.com", "홍길동");

        verify(mailSender, times(2)).send(message);
    }
}
