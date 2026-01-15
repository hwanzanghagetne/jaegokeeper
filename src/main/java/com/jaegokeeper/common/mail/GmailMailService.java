package com.jaegokeeper.common.mail;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GmailMailService implements MailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendSignupCode(String to, String code) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("[JaegoKeeper] 회원가입 이메일 인증번호");
        msg.setText("인증번호: " + code);
        mailSender.send(msg);
    }

    @Override
    public void sedWelcome(String to, String name) {
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(to);
        msg.setSubject("[JaegoKeeper] 환영합니다!");
        msg.setText(name + "님 환영합니다. 앞으로 잘 부탁드립니다.");
        mailSender.send(msg);
    }
}
