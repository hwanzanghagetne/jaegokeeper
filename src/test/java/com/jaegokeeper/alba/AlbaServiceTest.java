package com.jaegokeeper.alba;

import com.jaegokeeper.alba.dto.AlbaDetailResponse;
import com.jaegokeeper.alba.dto.AlbaRegisterRequest;
import com.jaegokeeper.alba.dto.AlbaUpdateRequest;
import com.jaegokeeper.alba.mapper.AlbaMapper;
import com.jaegokeeper.alba.model.Alba;
import com.jaegokeeper.alba.service.AlbaService;
import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.mail.MailService;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.exception.ErrorCode;
import com.jaegokeeper.image.dto.ImageInfoDTO;
import com.jaegokeeper.image.service.ImageService;
import com.jaegokeeper.schedule.mapper.ScheduleMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class AlbaServiceTest {

    @InjectMocks
    private AlbaService albaService;

    @Mock
    private AlbaMapper albaMapper;

    @Mock
    private ScheduleMapper scheduleMapper;

    @Mock
    private ImageService imageService;

    @Mock
    private MailService mailService;

    private final LoginContext login = new LoginContext(10, 1, "tester", "LOCAL");

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void 알바수정_0건영향_예외() {
        AlbaUpdateRequest req = new AlbaUpdateRequest();
        req.setAlbaId(5);

        when(albaMapper.countByStoreIdAndAlbaId(1, 5)).thenReturn(1);
        when(albaMapper.updateAlba(1, req)).thenReturn(0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> albaService.updateAlba(login, req));

        assertEquals(ErrorCode.STATE_CONFLICT, e.getErrorCode());
    }

    @Test
    public void 알바수정_내매장_알바가_아니면_NOT_FOUND() {
        AlbaUpdateRequest req = new AlbaUpdateRequest();
        req.setAlbaId(5);

        when(albaMapper.countByStoreIdAndAlbaId(1, 5)).thenReturn(0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> albaService.updateAlba(login, req));

        assertEquals(ErrorCode.ALBA_NOT_FOUND, e.getErrorCode());
        verify(albaMapper, never()).updateAlba(any(Integer.class), any());
    }

    @Test
    public void 알바수정_다른_알바의_전화번호면_중복_예외() {
        AlbaUpdateRequest req = new AlbaUpdateRequest();
        req.setAlbaId(5);
        req.setAlbaPhone("010-1111-2222");

        when(albaMapper.countByStoreIdAndAlbaId(1, 5)).thenReturn(1);
        when(albaMapper.existsByAlbaPhone(1, "010-1111-2222", 5)).thenReturn(1);

        BusinessException e = assertThrows(BusinessException.class,
                () -> albaService.updateAlba(login, req));

        assertEquals(ErrorCode.ALBA_PHONE_DUPLICATE, e.getErrorCode());
    }

    @Test
    public void 알바수정_다른_알바의_이메일이면_중복_예외_이메일은_소문자로_비교() {
        AlbaUpdateRequest req = new AlbaUpdateRequest();
        req.setAlbaId(5);
        req.setAlbaEmail("Alba@Example.com");

        when(albaMapper.countByStoreIdAndAlbaId(1, 5)).thenReturn(1);
        when(albaMapper.existsByAlbaEmail(1, "alba@example.com", 5)).thenReturn(1);

        BusinessException e = assertThrows(BusinessException.class,
                () -> albaService.updateAlba(login, req));

        assertEquals(ErrorCode.ALBA_EMAIL_DUPLICATE, e.getErrorCode());
    }

    @Test
    public void 알바수정_정상() {
        AlbaUpdateRequest req = new AlbaUpdateRequest();
        req.setAlbaId(5);
        req.setAlbaName("새이름");

        when(albaMapper.countByStoreIdAndAlbaId(1, 5)).thenReturn(1);
        when(albaMapper.updateAlba(1, req)).thenReturn(1);

        albaService.updateAlba(login, req);

        verify(albaMapper).updateAlba(1, req);
    }

    @Test
    public void 알바삭제_대상없으면_NOT_FOUND() {
        when(albaMapper.deleteAlba(1, 5)).thenReturn(0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> albaService.deleteAlba(login, 5));

        assertEquals(ErrorCode.ALBA_NOT_FOUND, e.getErrorCode());
    }

    @Test
    public void 알바상세_다른_매장이면_NOT_FOUND() {
        when(albaMapper.getAlbaById(1, 5)).thenReturn(null);

        BusinessException e = assertThrows(BusinessException.class,
                () -> albaService.getAlbaById(login, 5));

        assertEquals(ErrorCode.ALBA_NOT_FOUND, e.getErrorCode());
    }

    @Test
    public void 알바상세_정상() {
        AlbaDetailResponse detail = new AlbaDetailResponse();
        when(albaMapper.getAlbaById(1, 5)).thenReturn(detail);

        assertEquals(detail, albaService.getAlbaById(login, 5));
    }

    @Test
    public void 알바등록_이메일있으면_환영메일발송() {
        AlbaRegisterRequest req = new AlbaRegisterRequest();
        req.setAlbaName("홍길동");
        req.setAlbaPhone("010-1234-5678");
        req.setAlbaEmail("alba@example.com");

        when(albaMapper.existsByStoreId(1)).thenReturn(true);
        when(albaMapper.existsByAlbaPhone(1, "010-1234-5678", null)).thenReturn(0);
        when(albaMapper.existsByAlbaEmail(1, "alba@example.com", null)).thenReturn(0);
        doAnswer(invocation -> {
            Alba arg = invocation.getArgument(0);
            arg.setAlbaId(100);
            return null;
        }).when(albaMapper).insertAlba(any(Alba.class));

        albaService.saveAlbaRegister(login, req);

        verify(mailService).sendWelcome("alba@example.com", "홍길동");
    }

    @Test
    public void 알바등록_이메일없으면_환영메일미발송() {
        AlbaRegisterRequest req = new AlbaRegisterRequest();
        req.setAlbaName("홍길동");
        req.setAlbaPhone("010-1234-5678");

        when(albaMapper.existsByStoreId(1)).thenReturn(true);
        when(albaMapper.existsByAlbaPhone(1, "010-1234-5678", null)).thenReturn(0);
        doAnswer(invocation -> {
            Alba arg = invocation.getArgument(0);
            arg.setAlbaId(101);
            return null;
        }).when(albaMapper).insertAlba(any(Alba.class));

        albaService.saveAlbaRegister(login, req);

        verify(mailService, never()).sendWelcome(any(), any());
    }

    private AlbaRegisterRequest registerRequest() {
        AlbaRegisterRequest req = new AlbaRegisterRequest();
        req.setAlbaName("홍길동");
        req.setAlbaPhone("010-1234-5678");
        return req;
    }

    private void stubInsertWithId(int albaId) {
        doAnswer(invocation -> {
            Alba arg = invocation.getArgument(0);
            arg.setAlbaId(albaId);
            return null;
        }).when(albaMapper).insertAlba(any(Alba.class));
    }

    private ImageInfoDTO uploaded(int imageId, String path) {
        ImageInfoDTO info = new ImageInfoDTO();
        info.setImageId(imageId);
        info.setImagePath(path);
        return info;
    }

    @Test
    public void 알바등록_매장은_세션에서_가져오고_파일이_없으면_이미지는_null() {
        when(albaMapper.existsByStoreId(1)).thenReturn(true);
        stubInsertWithId(100);

        albaService.saveAlbaRegister(login, registerRequest());

        ArgumentCaptor<Alba> captor = ArgumentCaptor.forClass(Alba.class);
        verify(albaMapper).insertAlba(captor.capture());
        assertEquals(Integer.valueOf(1), captor.getValue().getStoreId());
        assertNull(captor.getValue().getImageId());
    }

    @Test
    public void 알바등록_파일이_있으면_업로드한_이미지_ID로_저장() {
        AlbaRegisterRequest req = registerRequest();
        req.setFile(new MockMultipartFile("file", "a.png", "image/png", new byte[]{1}));
        when(albaMapper.existsByStoreId(1)).thenReturn(true);
        when(imageService.uploadIfPresent(req.getFile())).thenReturn(uploaded(9, "2026/11/10/a.png"));
        stubInsertWithId(100);

        albaService.saveAlbaRegister(login, req);

        ArgumentCaptor<Alba> captor = ArgumentCaptor.forClass(Alba.class);
        verify(albaMapper).insertAlba(captor.capture());
        assertEquals(Integer.valueOf(9), captor.getValue().getImageId());
    }

    @Test
    public void 알바등록_전화번호_중복이면_예외() {
        when(albaMapper.existsByStoreId(1)).thenReturn(true);
        when(albaMapper.existsByAlbaPhone(1, "010-1234-5678", null)).thenReturn(1);

        BusinessException e = assertThrows(BusinessException.class,
                () -> albaService.saveAlbaRegister(login, registerRequest()));

        assertEquals(ErrorCode.ALBA_PHONE_DUPLICATE, e.getErrorCode());
        verifyNoInteractions(imageService);
        verify(albaMapper, never()).insertAlba(any());
    }

    @Test
    public void 알바등록_이메일_중복이면_예외() {
        AlbaRegisterRequest req = registerRequest();
        req.setAlbaEmail("Alba@Example.com");
        when(albaMapper.existsByStoreId(1)).thenReturn(true);
        when(albaMapper.existsByAlbaEmail(1, "alba@example.com", null)).thenReturn(1);

        BusinessException e = assertThrows(BusinessException.class,
                () -> albaService.saveAlbaRegister(login, req));

        assertEquals(ErrorCode.ALBA_EMAIL_DUPLICATE, e.getErrorCode());
        verifyNoInteractions(imageService);
    }

    @Test
    public void 알바등록_저장_실패시_올린_이미지_삭제() {
        AlbaRegisterRequest req = registerRequest();
        when(albaMapper.existsByStoreId(1)).thenReturn(true);
        when(imageService.uploadIfPresent(any())).thenReturn(uploaded(9, "2026/11/10/a.png"));

        BusinessException e = assertThrows(BusinessException.class,
                () -> albaService.saveAlbaRegister(login, req));

        assertEquals(ErrorCode.INTERNAL_ERROR, e.getErrorCode());
        verify(imageService).deleteImageFile("2026/11/10/a.png");
    }

    @Test
    public void 알바등록_스케줄_중복키_예외면_올린_이미지_삭제() {
        AlbaRegisterRequest req = registerRequest();
        req.setScheduleTimes(java.util.List.of("월"));
        when(albaMapper.existsByStoreId(1)).thenReturn(true);
        when(imageService.uploadIfPresent(any())).thenReturn(uploaded(9, "2026/11/10/a.png"));
        stubInsertWithId(100);
        doThrow(new org.springframework.dao.DuplicateKeyException("dup"))
                .when(scheduleMapper).insertSchedule(any());

        BusinessException e = assertThrows(BusinessException.class,
                () -> albaService.saveAlbaRegister(login, req));

        assertEquals(ErrorCode.SCHEDULE_TIME_DUPLICATE, e.getErrorCode());
        verify(imageService).deleteImageFile("2026/11/10/a.png");
    }

    @Test
    public void 알바등록_동시_등록으로_전화번호_중복키_예외면_전화번호_중복_예외와_이미지_삭제() {
        AlbaRegisterRequest req = registerRequest();
        when(albaMapper.existsByStoreId(1)).thenReturn(true);
        when(imageService.uploadIfPresent(any())).thenReturn(uploaded(9, "2026/11/10/a.png"));
        doThrow(new org.springframework.dao.DuplicateKeyException("dup")).when(albaMapper).insertAlba(any(Alba.class));
        when(albaMapper.existsByAlbaPhone(1, "010-1234-5678", null)).thenReturn(0, 1);

        BusinessException e = assertThrows(BusinessException.class,
                () -> albaService.saveAlbaRegister(login, req));

        assertEquals(ErrorCode.ALBA_PHONE_DUPLICATE, e.getErrorCode());
        verify(imageService).deleteImageFile("2026/11/10/a.png");
    }

    @Test
    public void 알바등록_동시_등록으로_이메일_중복키_예외면_이메일_중복_예외() {
        AlbaRegisterRequest req = registerRequest();
        req.setAlbaEmail("alba@example.com");
        when(albaMapper.existsByStoreId(1)).thenReturn(true);
        doThrow(new org.springframework.dao.DuplicateKeyException("dup")).when(albaMapper).insertAlba(any(Alba.class));

        BusinessException e = assertThrows(BusinessException.class,
                () -> albaService.saveAlbaRegister(login, req));

        assertEquals(ErrorCode.ALBA_EMAIL_DUPLICATE, e.getErrorCode());
    }
}
