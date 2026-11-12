package com.jaegokeeper.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class GmailMailService implements MailService {

    private static final int MAX_ATTEMPTS = 2;

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine mailTemplateEngine;

    @Async("asyncExecutor")
    @Override
    public void sendSignupCode(String to, String code) {
        send(to, "[JaegoKeeper] 회원가입 이메일 인증번호", "signup", Map.of("code", code));
    }

    @Async("asyncExecutor")
    @Override
    public void sendWelcome(String to, String name) {
        send(to, "[JaegoKeeper] 환영합니다!", "welcome", Map.of("name", name));
    }

    private void send(String to, String subject, String template, Map<String, Object> values) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(renderTemplate(template, values), true);
            sendWithRetry(message, to);
        } catch (MessagingException e) {
            log.error("[GmailMailService] 메시지 생성 실패: {}", to, e);
        }
    }

    private void sendWithRetry(MimeMessage message, String to) {
        for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
            try {
                mailSender.send(message);
                log.info("[GmailMailService] 메일 전송 성공: {}", to);
                return;
            } catch (Exception e) {
                log.warn("[GmailMailService] 메일 전송 실패 (시도 {}/{}): {}", attempt, MAX_ATTEMPTS, to);
                if (attempt == MAX_ATTEMPTS) {
                    log.error("[GmailMailService] 메일 전송 최종 실패: {}", to, e);
                }
            }
        }
    }

    private String renderTemplate(String template, Map<String, Object> values) {
        Context context = new Context();
        context.setVariables(values);
        return mailTemplateEngine.process(template, context);
    }
}
