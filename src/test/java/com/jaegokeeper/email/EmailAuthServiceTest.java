package com.jaegokeeper.email;

import com.jaegokeeper.mail.MailService;
import com.jaegokeeper.email.mapper.EmailAuthMapper;
import com.jaegokeeper.email.service.EmailAuthService;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.exception.ErrorCode;
import com.jaegokeeper.user.mapper.UserMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

        BusinessException e = assertThrows(BusinessException.class,
                () -> emailAuthService.assertVerified("test@example.com"));

        assertEquals(ErrorCode.EMAIL_NOT_VERIFIED, e.getErrorCode());
    }

    @Test
    public void 이메일_인증여부가_0이면_미인증_예외() {
        when(emailAuthMapper.isVerified("test@example.com")).thenReturn(0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> emailAuthService.assertVerified("test@example.com"));

        assertEquals(ErrorCode.EMAIL_NOT_VERIFIED, e.getErrorCode());
    }

    @Test
    public void 이메일_인증여부가_1이면_정상통과() {
        when(emailAuthMapper.isVerified(anyString())).thenReturn(1);

        emailAuthService.assertVerified("test@example.com");

        verify(emailAuthMapper).isVerified("test@example.com");
    }

    @Test
    public void 인증번호_발송_이미_가입된_이메일이면_예외이고_메일을_보내지_않음() {
        when(usrMapper.countByEmail("test@example.com")).thenReturn(1);

        BusinessException e = assertThrows(BusinessException.class,
                () -> emailAuthService.sendCode("test@example.com"));

        assertEquals(ErrorCode.EMAIL_ALREADY_EXISTS, e.getErrorCode());
        verify(emailAuthMapper, never()).upsertAuthCode(anyString(), anyString());
        verify(mailService, never()).sendSignupCode(anyString(), anyString());
    }

    @Test
    public void 인증번호_발송_6자리_코드를_저장하고_같은_코드를_메일로_보냄() {
        when(usrMapper.countByEmail("test@example.com")).thenReturn(0);

        emailAuthService.sendCode("test@example.com");

        ArgumentCaptor<String> stored = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> sent = ArgumentCaptor.forClass(String.class);
        verify(emailAuthMapper).upsertAuthCode(eq("test@example.com"), stored.capture());
        verify(mailService).sendSignupCode(eq("test@example.com"), sent.capture());
        assertTrue(stored.getValue().matches("\\d{6}"));
        assertEquals(stored.getValue(), sent.getValue());
    }

    @Test
    public void 인증번호_확인_일치하면_통과() {
        when(emailAuthMapper.verifyCode("test@example.com", "123456")).thenReturn(1);

        emailAuthService.verifyCode("test@example.com", "123456");

        verify(emailAuthMapper).verifyCode("test@example.com", "123456");
    }

    @Test
    public void 인증번호_확인_틀리거나_만료되면_예외() {
        when(emailAuthMapper.verifyCode(any(), any())).thenReturn(0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> emailAuthService.verifyCode("test@example.com", "000000"));

        assertEquals(ErrorCode.EMAIL_CODE_INVALID, e.getErrorCode());
    }
}
