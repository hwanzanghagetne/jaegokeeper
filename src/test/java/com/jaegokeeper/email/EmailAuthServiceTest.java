package com.jaegokeeper.email;

import com.jaegokeeper.common.mail.MailService;
import com.jaegokeeper.email.mapper.EmailAuthMapper;
import com.jaegokeeper.email.service.EmailAuthService;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.exception.ErrorCode;
import com.jaegokeeper.user.mapper.UserMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class EmailAuthServiceTest {

    @InjectMocks
    private EmailAuthService emailAuthService;

    @Mock
    private EmailAuthMapper emailAuthMapper;

    @Mock
    private UserMapper usrMapper;

    @Mock
    private MailService mailService;

    @Test
    public void 이메일_인증여부_조회결과_null이면_미인증_예외() {
        when(emailAuthMapper.isVerified(anyString())).thenReturn(null);

        try {
            emailAuthService.assertVerified("test@example.com");
            fail("BusinessException이 발생해야 합니다");
        } catch (BusinessException e) {
            assertEquals(ErrorCode.EMAIL_NOT_VERIFIED, e.getErrorCode());
        }
    }

    @Test
    public void 이메일_인증여부가_1이면_정상통과() {
        when(emailAuthMapper.isVerified(anyString())).thenReturn(1);

        emailAuthService.assertVerified("test@example.com");
    }
}
