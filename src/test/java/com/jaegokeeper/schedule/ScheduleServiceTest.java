package com.jaegokeeper.schedule;

import com.jaegokeeper.alba.mapper.AlbaMapper;
import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.exception.ErrorCode;
import com.jaegokeeper.schedule.dto.ScheduleRegisterRequest;
import com.jaegokeeper.schedule.dto.ScheduleUpdateRequest;
import com.jaegokeeper.schedule.dto.WorkInOutRequest;
import com.jaegokeeper.schedule.mapper.ScheduleMapper;
import com.jaegokeeper.schedule.service.ScheduleService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.dao.DuplicateKeyException;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ScheduleServiceTest {

    @InjectMocks
    private ScheduleService scheduleService;

    @Mock
    private ScheduleMapper scheduleMapper;

    @Mock
    private AlbaMapper albaMapper;

    private final LoginContext login = new LoginContext(10, 1, "tester", "LOCAL");

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void 스케줄등록_요일은_정규화해서_저장() {
        ScheduleRegisterRequest req = new ScheduleRegisterRequest();
        req.setAlbaId(7);
        req.setScheduleTime("월");

        when(albaMapper.countByStoreIdAndAlbaId(1, 7)).thenReturn(1);

        scheduleService.saveScheduleRegister(login, req);

        assertEquals("MONDAY", req.getScheduleTime());
        verify(scheduleMapper).insertSchedule(req);
    }

    @Test
    public void 스케줄등록_잘못된_요일이면_BAD_REQUEST() {
        ScheduleRegisterRequest req = new ScheduleRegisterRequest();
        req.setAlbaId(7);
        req.setScheduleTime("FUNDAY");

        when(albaMapper.countByStoreIdAndAlbaId(1, 7)).thenReturn(1);

        BusinessException e = assertThrows(BusinessException.class,
                () -> scheduleService.saveScheduleRegister(login, req));

        assertEquals(ErrorCode.BAD_REQUEST, e.getErrorCode());
    }

    @Test
    public void 스케줄등록_내매장_알바가_아니면_NOT_FOUND() {
        ScheduleRegisterRequest req = new ScheduleRegisterRequest();
        req.setAlbaId(7);
        req.setScheduleTime("MONDAY");

        when(albaMapper.countByStoreIdAndAlbaId(1, 7)).thenReturn(0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> scheduleService.saveScheduleRegister(login, req));

        assertEquals(ErrorCode.ALBA_NOT_FOUND, e.getErrorCode());
    }

    @Test
    public void 스케줄등록_같은_요일이_있으면_중복_예외() {
        ScheduleRegisterRequest req = new ScheduleRegisterRequest();
        req.setAlbaId(7);
        req.setScheduleTime("MONDAY");

        when(albaMapper.countByStoreIdAndAlbaId(1, 7)).thenReturn(1);
        when(scheduleMapper.insertSchedule(req)).thenThrow(new DuplicateKeyException("dup"));

        BusinessException e = assertThrows(BusinessException.class,
                () -> scheduleService.saveScheduleRegister(login, req));

        assertEquals(ErrorCode.SCHEDULE_TIME_DUPLICATE, e.getErrorCode());
    }

    @Test
    public void 스케줄수정_대상없으면_NOT_FOUND() {
        ScheduleUpdateRequest req = new ScheduleUpdateRequest();
        req.setScheduleId(3);
        req.setScheduleTime("TUE");

        when(scheduleMapper.updateSchedule(1, req)).thenReturn(0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> scheduleService.updateSchedule(login, req));

        assertEquals(ErrorCode.SCHEDULE_NOT_FOUND, e.getErrorCode());
        assertEquals("TUESDAY", req.getScheduleTime());
    }

    @Test
    public void 스케줄수정_같은_요일이_있으면_중복_예외() {
        ScheduleUpdateRequest req = new ScheduleUpdateRequest();
        req.setScheduleId(3);
        req.setScheduleTime("MONDAY");

        when(scheduleMapper.updateSchedule(1, req)).thenThrow(new DuplicateKeyException("dup"));

        BusinessException e = assertThrows(BusinessException.class,
                () -> scheduleService.updateSchedule(login, req));

        assertEquals(ErrorCode.SCHEDULE_TIME_DUPLICATE, e.getErrorCode());
    }

    @Test
    public void 날짜별_스케줄조회는_요일로_변환해서_조회() {
        LocalDate monday = LocalDate.of(2026, 11, 9);

        scheduleService.getScheduleListByDate(login, monday);

        verify(scheduleMapper).selectSchedulesWithWorkByDate(1, monday, "MONDAY");
    }

    @Test
    public void 출근기록_이미_열린_기록이_있으면_충돌_예외() {
        WorkInOutRequest req = new WorkInOutRequest();
        req.setAlbaId(7);

        when(albaMapper.countByStoreIdAndAlbaId(1, 7)).thenReturn(1);
        when(scheduleMapper.insertWorkIn(any(WorkInOutRequest.class))).thenReturn(0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> scheduleService.recordWorkIn(login, req));

        assertEquals(ErrorCode.STATE_CONFLICT, e.getErrorCode());
    }

    @Test
    public void 퇴근처리_0건영향_예외() {
        WorkInOutRequest req = new WorkInOutRequest();
        req.setAlbaId(7);

        when(albaMapper.countByStoreIdAndAlbaId(1, 7)).thenReturn(1);
        when(scheduleMapper.updateWorkOut(any(WorkInOutRequest.class))).thenReturn(0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> scheduleService.recordWorkOut(login, req));

        assertEquals(ErrorCode.STATE_CONFLICT, e.getErrorCode());
    }
}
