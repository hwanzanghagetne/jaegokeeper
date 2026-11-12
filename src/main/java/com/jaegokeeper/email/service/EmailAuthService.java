package com.jaegokeeper.email.service;

import com.jaegokeeper.email.mapper.EmailAuthMapper;
import com.jaegokeeper.mail.MailService;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

import static com.jaegokeeper.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class EmailAuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final EmailAuthMapper emailAuthMapper;
    private final UserMapper userMapper;
    private final MailService mailService;

    public void sendCode(String email) {
        String normalized = normalize(email);
        if (userMapper.countByEmail(normalized) > 0) {
            throw new BusinessException(EMAIL_ALREADY_EXISTS);
        }

        String code = generate6DigitCode();

        emailAuthMapper.upsertAuthCode(normalized, code);
        mailService.sendSignupCode(normalized, code);
    }

    public void verifyCode(String email, String code) {
        int updated = emailAuthMapper.verifyCode(normalize(email), code);
        if (updated != 1) {
            throw new BusinessException(EMAIL_CODE_INVALID);
        }
    }

    public void assertVerified(String email) {
        Integer verified = emailAuthMapper.isVerified(normalize(email));
        if (verified == null || verified != 1) {
            throw new BusinessException(EMAIL_NOT_VERIFIED);
        }
    }

    private String normalize(String email) {
        return email.trim().toLowerCase();
    }

    private String generate6DigitCode() {
        int n = SECURE_RANDOM.nextInt(1_000_000);
        return String.format("%06d", n);
    }
}
