package com.jaegokeeper.schedule;

import com.jaegokeeper.alba.mapper.AlbaMapper;
import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.exception.ErrorCode;
import com.jaegokeeper.schedule.dto.ScheduleUpdateRequest;
import com.jaegokeeper.schedule.dto.WorkInOutRequest;
import com.jaegokeeper.schedule.mapper.ScheduleMapper;
import com.jaegokeeper.schedule.service.ScheduleService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class ScheduleServiceTest {

    @InjectMocks
    private ScheduleService scheduleService;

    @Mock
    private ScheduleMapper scheduleMapper;

    @Mock
    private AlbaMapper albaMapper;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void 스케줄수정_0건영향_예외() {
        LoginContext login = new LoginContext(10, 1, "tester", "LOCAL");
        ScheduleUpdateRequest req = new ScheduleUpdateRequest();
        req.setScheduleId(3);

        when(scheduleMapper.existsScheduleById(3)).thenReturn(true);
        when(scheduleMapper.countByStoreIdAndScheduleId(1, 3)).thenReturn(1);
        when(scheduleMapper.updateSchedule(any(ScheduleUpdateRequest.class))).thenReturn(0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> scheduleService.updateSchedule(login, 1, req));

        assertEquals(ErrorCode.STATE_CONFLICT, e.getErrorCode());
    }

    @Test
    public void 퇴근처리_0건영향_예외() {
        LoginContext login = new LoginContext(10, 1, "tester", "LOCAL");
        WorkInOutRequest req = new WorkInOutRequest();
        req.setAlbaId(7);

        when(albaMapper.existsAlbaById(7)).thenReturn(true);
        when(albaMapper.countByStoreIdAndAlbaId(1, 7)).thenReturn(1);
        when(scheduleMapper.updateWorkOut(any(WorkInOutRequest.class))).thenReturn(0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> scheduleService.recordWorkOut(login, 1, req));

        assertEquals(ErrorCode.STATE_CONFLICT, e.getErrorCode());
    }
}
