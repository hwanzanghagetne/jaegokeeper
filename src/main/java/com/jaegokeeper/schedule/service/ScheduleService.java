package com.jaegokeeper.schedule.service;

import com.jaegokeeper.alba.mapper.AlbaMapper;
import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.common.DateTimeConstants;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.schedule.dto.ScheduleDetailResponse;
import com.jaegokeeper.schedule.dto.ScheduleListResponse;
import com.jaegokeeper.schedule.dto.ScheduleRegisterRequest;
import com.jaegokeeper.schedule.dto.ScheduleUpdateRequest;
import com.jaegokeeper.schedule.dto.WorkInOutRequest;
import com.jaegokeeper.schedule.dto.WorkTimeResponse;
import com.jaegokeeper.schedule.mapper.ScheduleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.jaegokeeper.exception.ErrorCode.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleMapper scheduleMapper;
    private final AlbaMapper albaMapper;

    @Transactional
    public int saveScheduleRegister(LoginContext login, ScheduleRegisterRequest req) {
        validateAlbaAccess(login, req.getAlbaId());
        return scheduleMapper.insertSchedule(req);
    }

    @Transactional
    public void updateSchedule(LoginContext login, ScheduleUpdateRequest req) {
        validateScheduleAccess(login, req.getScheduleId());
        int updated = scheduleMapper.updateSchedule(req);
        if (updated == 0) {
            throw new BusinessException(STATE_CONFLICT);
        }
    }

    public List<ScheduleDetailResponse> getScheduleByDate(String date) {
        DayOfWeek dayOfWeek = LocalDate.parse(date).getDayOfWeek();
        return scheduleMapper.selectSchedulesByDate(dayOfWeek.toString());
    }

    public List<ScheduleListResponse> getScheduleListByDate(LoginContext login, String date) {
        DayOfWeek dayOfWeek = LocalDate.parse(date).getDayOfWeek();
        return scheduleMapper.selectSchedulesWithWorkByDate(login.getStoreId(), date, dayOfWeek.toString());
    }

    public List<WorkTimeResponse> selectWorkTime(LoginContext login, int albaId, LocalDate date) {
        validateAlbaAccess(login, albaId);
        return scheduleMapper.selectWorkTime(login.getStoreId(), albaId, date);
    }

    public void recordWorkIn(LoginContext login, WorkInOutRequest req) {
        validateAlbaAccess(login, req.getAlbaId());
        req.setWorkIn(LocalDateTime.now(DateTimeConstants.KST));
        req.setWorkDate(LocalDate.now(DateTimeConstants.KST));
        scheduleMapper.insertWorkIn(req);
    }

    public void recordWorkOut(LoginContext login, WorkInOutRequest req) {
        validateAlbaAccess(login, req.getAlbaId());
        req.setWorkOut(LocalDateTime.now(DateTimeConstants.KST));
        req.setWorkDate(LocalDate.now(DateTimeConstants.KST));
        int updated = scheduleMapper.updateWorkOut(req);
        if (updated == 0) {
            throw new BusinessException(STATE_CONFLICT);
        }
    }

    private void validateAlbaAccess(LoginContext login, Integer albaId) {
        if (albaId == null) {
            throw new BusinessException(BAD_REQUEST);
        }
        if (!albaMapper.existsAlbaById(albaId)) {
            throw new BusinessException(ALBA_NOT_FOUND);
        }
        if (albaMapper.countByStoreIdAndAlbaId(login.getStoreId(), albaId) == 0) {
            throw new BusinessException(FORBIDDEN);
        }
    }

    private void validateScheduleAccess(LoginContext login, Integer scheduleId) {
        if (scheduleId == null) {
            throw new BusinessException(BAD_REQUEST);
        }
        if (!scheduleMapper.existsScheduleById(scheduleId)) {
            throw new BusinessException(SCHEDULE_NOT_FOUND);
        }
        if (scheduleMapper.countByStoreIdAndScheduleId(login.getStoreId(), scheduleId) == 0) {
            throw new BusinessException(FORBIDDEN);
        }
    }
}
