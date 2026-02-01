package com.jaegokeeper.email.service;

public interface EmailAuthService {

    /*인증번호 발송*/
    void sendCode(String email);

    /*인증번호 검증*/
    void verifyCode(String email, String code);

    /*회원가입 직전 인증 여부 확인*/
    void assertVerified(String email);
}
