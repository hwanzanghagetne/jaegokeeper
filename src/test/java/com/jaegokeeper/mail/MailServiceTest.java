package com.jaegokeeper.mail;

import com.jaegokeeper.common.mail.MailService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:test-mail-context.xml")
public class MailServiceTest {

    @Autowired
    private MailService mailService;

    @Test
    public void sendSignupCodeMail() {
        mailService.sendSignupCode("dltmdghks543@gmail.com", "123456");
    }

    @Test
    public void sendWelcomeMail() {
        mailService.sendWelcome("dltmdghks543@gmail.com", "승환");
    }
}
