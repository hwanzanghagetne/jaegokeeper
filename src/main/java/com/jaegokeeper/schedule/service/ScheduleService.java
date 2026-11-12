package com.jaegokeeper.schedule.service;

import com.jaegokeeper.alba.mapper.AlbaMapper;
import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.common.DateTimeConstants;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.schedule.ScheduleTimes;
import com.jaegokeeper.schedule.dto.ScheduleListResponse;
import com.jaegokeeper.schedule.dto.ScheduleRegisterRequest;
import com.jaegokeeper.schedule.dto.ScheduleUpdateRequest;
import com.jaegokeeper.schedule.dto.WorkInOutRequest;
import com.jaegokeeper.schedule.dto.WorkTimeResponse;
import com.jaegokeeper.schedule.mapper.ScheduleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.jaegokeeper.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleMapper scheduleMapper;
    private final AlbaMapper albaMapper;

    @Transactional
    public int saveScheduleRegister(LoginContext login, ScheduleRegisterRequest req) {
        validateAlbaAccess(login, req.getAlbaId());
        req.setScheduleTime(ScheduleTimes.normalize(req.getScheduleTime()));
        try {
            return scheduleMapper.insertSchedule(req);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(SCHEDULE_TIME_DUPLICATE);
        }
    }

    @Transactional
    public void updateSchedule(LoginContext login, ScheduleUpdateRequest req) {
        req.setScheduleTime(ScheduleTimes.normalize(req.getScheduleTime()));
        int updated;
        try {
            updated = scheduleMapper.updateSchedule(login.getStoreId(), req);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(SCHEDULE_TIME_DUPLICATE);
        }
        if (updated == 0) {
            throw new BusinessException(SCHEDULE_NOT_FOUND);
        }
    }

    @Transactional(readOnly = true)
    public List<ScheduleListResponse> getScheduleListByDate(LoginContext login, LocalDate date) {
        return scheduleMapper.selectSchedulesWithWorkByDate(login.getStoreId(), date, date.getDayOfWeek().name());
    }

    @Transactional(readOnly = true)
    public List<WorkTimeResponse> selectWorkTime(LoginContext login, int albaId, LocalDate date) {
        validateAlbaAccess(login, albaId);
        return scheduleMapper.selectWorkTime(login.getStoreId(), albaId, date);
    }

    public void recordWorkIn(LoginContext login, WorkInOutRequest req) {
        validateAlbaAccess(login, req.getAlbaId());
        req.setWorkIn(LocalDateTime.now(DateTimeConstants.KST));
        req.setWorkDate(LocalDate.now(DateTimeConstants.KST));
        if (scheduleMapper.insertWorkIn(req) == 0) {
            throw new BusinessException(STATE_CONFLICT);
        }
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
        if (albaMapper.countByStoreIdAndAlbaId(login.getStoreId(), albaId) == 0) {
            throw new BusinessException(ALBA_NOT_FOUND);
        }
    }
}
