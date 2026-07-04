package com.jaegokeeper.alba;

import com.jaegokeeper.alba.dto.AlbaRegisterRequest;
import com.jaegokeeper.alba.dto.AlbaUpdateRequest;
import com.jaegokeeper.alba.mapper.AlbaMapper;
import com.jaegokeeper.alba.mapper.WorkMapper;
import com.jaegokeeper.alba.service.AlbaService;
import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.auth.utils.StoreAccessValidator;
import com.jaegokeeper.mail.MailService;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.exception.ErrorCode;
import com.jaegokeeper.image.service.ImageService;
import com.jaegokeeper.schedule.mapper.ScheduleMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AlbaServiceTest {

    @InjectMocks
    private AlbaService albaService;

    @Mock
    private AlbaMapper albaMapper;

    @Mock
    private WorkMapper workMapper;

    @Mock
    private ScheduleMapper scheduleMapper;

    @Mock
    private ImageService imageService;

    @Mock
    private MailService mailService;

    @Mock
    private StoreAccessValidator storeAccessValidator;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void 알바수정_0건영향_예외() {
        LoginContext login = new LoginContext(10, 1, "tester", "LOCAL");
        AlbaUpdateRequest req = new AlbaUpdateRequest();
        req.setAlbaId(5);

        when(albaMapper.existsAlbaById(5)).thenReturn(true);
        when(albaMapper.countByStoreIdAndAlbaId(1, 5)).thenReturn(1);
        when(albaMapper.updateAlba(any(AlbaUpdateRequest.class))).thenReturn(0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> albaService.updateAlba(login, 1, req));

        assertEquals(ErrorCode.STATE_CONFLICT, e.getErrorCode());
    }

    @Test
    public void 알바등록_이메일있으면_환영메일발송() {
        LoginContext login = new LoginContext(10, 1, "tester", "LOCAL");
        AlbaRegisterRequest req = new AlbaRegisterRequest();
        req.setAlbaName("홍길동");
        req.setAlbaPhone("010-1234-5678");
        req.setAlbaEmail("alba@example.com");

        when(albaMapper.existsByStoreId(1)).thenReturn(true);
        when(albaMapper.existsByAlbaPhone(1, "010-1234-5678")).thenReturn(0);
        when(albaMapper.existsByAlbaEmail(1, "alba@example.com")).thenReturn(0);
        doAnswer(invocation -> {
            AlbaRegisterRequest arg = invocation.getArgument(0);
            arg.setAlbaId(100);
            return 1;
        }).when(albaMapper).insertAlba(any(AlbaRegisterRequest.class));

        albaService.saveAlbaRegister(login, 1, req);

        verify(mailService).sendWelcome("alba@example.com", "홍길동");
    }

    @Test
    public void 알바등록_이메일없으면_환영메일미발송() {
        LoginContext login = new LoginContext(10, 1, "tester", "LOCAL");
        AlbaRegisterRequest req = new AlbaRegisterRequest();
        req.setAlbaName("홍길동");
        req.setAlbaPhone("010-1234-5678");

        when(albaMapper.existsByStoreId(1)).thenReturn(true);
        when(albaMapper.existsByAlbaPhone(1, "010-1234-5678")).thenReturn(0);
        doAnswer(invocation -> {
            AlbaRegisterRequest arg = invocation.getArgument(0);
            arg.setAlbaId(101);
            return 1;
        }).when(albaMapper).insertAlba(any(AlbaRegisterRequest.class));

        albaService.saveAlbaRegister(login, 1, req);

        verify(mailService, never()).sendWelcome(any(), any());
    }
}
