package com.jaegokeeper.common.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class GmailMailService implements MailService {

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine mailTemplateEngine;

    @Override
    public void sendSignupCode(String to, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message,false, "UTF-8");

            helper.setTo(to);
            helper.setSubject("[JaegoKeeper] 회원가입 이메일 인증번호");
            helper.setText(renderTemplate("signup", Map.of("code",code)), true);

            mailSender.send(message);
            log.info("[GmailMailService] 인증 메일 전송 성공: {}", to);

        } catch (MessagingException e) {
            log.error("[GmailMailService] 인증 메일 전송 실패: {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendWelcome(String to, String name) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");

            helper.setTo(to);
            helper.setSubject("[JaegoKeeper] 환영합니다!");
            helper.setText(renderTemplate("welcome", Map.of("name", name)), true);

            mailSender.send(message);
            log.info("[GmailMailService] 환영 메일 전송 성공: {}", to);
        } catch (MessagingException e) {
            log.error("[GmailMailService] 환영 메일 전송 실패: {}", e.getMessage());
            throw new RuntimeException(e);
        }

    }

    private String renderTemplate(String template, Map<String,Object> values) {
        Context context = new Context();
        context.setVariables(values);
        return mailTemplateEngine.process(template, context);
    }
}
