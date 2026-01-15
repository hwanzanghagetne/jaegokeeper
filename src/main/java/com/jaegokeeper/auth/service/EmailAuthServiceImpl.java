package com.jaegokeeper.auth.service;

import com.jaegokeeper.auth.mapper.EmailAuthMapper;
import com.jaegokeeper.common.mail.MailService;
import com.jaegokeeper.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class EmailAuthServiceImpl implements EmailAuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final EmailAuthMapper emailAuthMapper;
    private final UserMapper userMapper;
    private final MailService mailService;

    /*코드 전송*/
    @Override
    public void sendCode(String email) {
        int exists = userMapper.countByEmail(email);
        if (exists > 0) {
            throw new IllegalArgumentException("이미 가입된 이메일 입니다.");
        }

        String code = generate6DigitCode();

        emailAuthMapper.upsertAuthCode(email, code);
        mailService.sendSignupCode(email, code);
    }

    /*코드 검증*/
    @Override
    public void verifyCode(String email, String code) {
        int updated = emailAuthMapper.verifyCode(email, code);
        if (updated != 1) {
            throw new IllegalArgumentException("인증번호가 올바르지 않거나 만료되었습니다.");
        }
    }

    /*인증 검증*/
    @Override
    public void assertVerified(String email) {
        int verified = emailAuthMapper.isVerified(email);
        if (verified != 1) {
            throw new IllegalStateException("이메일 인증이 필요합니다.");
        }
    }

    /*코드 생성*/
    private String generate6DigitCode() {
        int n = SECURE_RANDOM.nextInt(1_000_000);
        return String.format("%06d", n);
    }
}
