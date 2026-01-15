package com.jaegokeeper.common.mail;

public interface MailService {

    void sendSignupCode(String to, String code);

    void sedWelcome(String to, String name);
}
