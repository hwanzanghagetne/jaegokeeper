package com.jaegokeeper.auth.service;

import com.jaegokeeper.auth.mapper.EmailAuthMapper;
import com.jaegokeeper.common.mail.MailService;
import com.jaegokeeper.hwan.exception.BusinessException;
import com.jaegokeeper.user.mapper.UsrMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

import static com.jaegokeeper.hwan.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class EmailAuthServiceImpl implements EmailAuthService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final EmailAuthMapper emailAuthMapper;
    private final UsrMapper usrMapper;
    private final MailService mailService;

    /*코드 전송*/
    @Override
    public void sendCode(String email) {
        int exists = usrMapper.countByEmail(email);
        if (exists > 0) {
            throw new BusinessException(EMAIL_ALREADY_REGISTERED);
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
            throw new BusinessException(EMAIL_CODE_INVALID);
        }
    }

    /*인증 검증*/
    @Override
    public void assertVerified(String email) {
        int verified = emailAuthMapper.isVerified(email);
        if (verified != 1) {
            throw new BusinessException(EMAIL_NOT_VERIFIED);
        }
    }

    /*코드 생성*/
    private String generate6DigitCode() {
        int n = SECURE_RANDOM.nextInt(1_000_000);
        return String.format("%06d", n);
    }
}
