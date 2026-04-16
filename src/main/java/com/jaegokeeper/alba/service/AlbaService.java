package com.jaegokeeper.alba.service;

import com.jaegokeeper.alba.dto.AlbaDetailResponse;
import com.jaegokeeper.alba.dto.AlbaListResponse;
import com.jaegokeeper.alba.dto.AlbaRegisterRequest;
import com.jaegokeeper.alba.dto.AlbaUpdateRequest;
import com.jaegokeeper.alba.mapper.AlbaMapper;
import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.image.dto.ImageInfoDTO;
import com.jaegokeeper.image.service.ImageService;
import com.jaegokeeper.schedule.dto.ScheduleRegisterRequest;
import com.jaegokeeper.schedule.mapper.ScheduleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static com.jaegokeeper.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class AlbaService {

    private final AlbaMapper albaMapper;
    private final ScheduleMapper scheduleMapper;
    private final ImageService imageService;

    @Transactional
    public int saveAlbaRegister(LoginContext login, AlbaRegisterRequest req) {
        return saveAlbaRegister(login, req.getStoreId(), req);
    }

    @Transactional
    public int saveAlbaRegister(LoginContext login, int storeId, AlbaRegisterRequest req) {
        validateStoreAccess(login, storeId);
        req.setStoreId(storeId);
        req.setAlbaEmail(normalizeOptionalEmail(req.getAlbaEmail()));
        if (!albaMapper.existsByStoreId(storeId)) {
            throw new BusinessException(STORE_NOT_FOUND);
        }
        if (albaMapper.existsByAlbaPhone(storeId, req.getAlbaPhone()) > 0) {
            throw new BusinessException(ALBA_PHONE_DUPLICATE);
        }
        if (req.getAlbaEmail() != null && albaMapper.existsByAlbaEmail(storeId, req.getAlbaEmail()) > 0) {
            throw new BusinessException(ALBA_EMAIL_DUPLICATE);
        }
        ImageInfoDTO imageDto = null;
        try {
            if (req.getFile() != null && !req.getFile().isEmpty()) {
                imageDto = new ImageInfoDTO();
                imageDto.setFile(req.getFile());
                int imageId = imageService.uploadImg(imageDto);
                req.setImageId(imageId);
            }

            albaMapper.insertAlba(req);
            if (req.getAlbaId() == null) {
                throw new BusinessException(INTERNAL_ERROR);
            }

            Set<String> scheduleTimes = normalizeScheduleTimes(req.getScheduleTimes());
            for (String scheduleTime : scheduleTimes) {
                ScheduleRegisterRequest scheduleReq = new ScheduleRegisterRequest();
                scheduleReq.setAlbaId(req.getAlbaId());
                scheduleReq.setScheduleTime(scheduleTime);
                scheduleMapper.insertSchedule(scheduleReq);
            }

            return req.getAlbaId();
        } catch (DuplicateKeyException e) {
            if (imageDto != null) imageService.deleteImageFile(imageDto.getImagePath());
            throw new BusinessException(SCHEDULE_TIME_DUPLICATE);
        } catch (DataIntegrityViolationException e) {
            if (imageDto != null) imageService.deleteImageFile(imageDto.getImagePath());
            throw new BusinessException(BAD_REQUEST);
        } catch (BusinessException e) {
            if (imageDto != null) imageService.deleteImageFile(imageDto.getImagePath());
            throw e;
        }
    }

    @Transactional
    public void updateAlba(LoginContext login, AlbaUpdateRequest req) {
        updateAlba(login, login.getStoreId(), req);
    }

    @Transactional
    public void updateAlba(LoginContext login, int storeId, AlbaUpdateRequest req) {
        validateAlbaAccess(login, storeId, req.getAlbaId());
        int updated = albaMapper.updateAlba(req);
        if (updated == 0) {
            throw new BusinessException(STATE_CONFLICT);
        }
    }

    @Transactional
    public void deleteAlba(LoginContext login, int albaId) {
        deleteAlba(login, login.getStoreId(), albaId);
    }

    @Transactional
    public void deleteAlba(LoginContext login, int storeId, int albaId) {
        validateAlbaAccess(login, storeId, albaId);
        int deleted = albaMapper.deleteAlba(albaId);
        if (deleted == 0) {
            throw new BusinessException(STATE_CONFLICT);
        }
    }

    @Transactional(readOnly = true)
    public AlbaDetailResponse getAlbaByStore(int storeId) {
        AlbaDetailResponse alba = albaMapper.getAlbaByStore(storeId);
        if (alba == null) {
            throw new BusinessException(ALBA_NOT_FOUND);
        }
        return alba;
    }

    @Transactional(readOnly = true)
    public AlbaDetailResponse getAlbaById(LoginContext login, int albaId) {
        return getAlbaById(login, login.getStoreId(), albaId);
    }

    @Transactional(readOnly = true)
    public AlbaDetailResponse getAlbaById(LoginContext login, int storeId, int albaId) {
        validateAlbaAccess(login, storeId, albaId);
        AlbaDetailResponse alba = albaMapper.getAlbaById(albaId);
        if (alba == null) {
            throw new BusinessException(ALBA_NOT_FOUND);
        }
        return alba;
    }

    @Transactional(readOnly = true)
    public List<AlbaListResponse> getAllAlbaList(LoginContext login, Integer storeId) {
        int targetStoreId = (storeId != null) ? storeId : login.getStoreId();
        validateStoreAccess(login, targetStoreId);
        return albaMapper.selectAllAlba(targetStoreId);
    }

    @Transactional(readOnly = true)
    public List<AlbaListResponse> getAllAlbaList(LoginContext login, int storeId) {
        return getAllAlbaList(login, Integer.valueOf(storeId));
    }

    private void validateStoreAccess(LoginContext login, Integer storeId) {
        if (storeId == null) {
            throw new BusinessException(BAD_REQUEST);
        }
        if (login.getStoreId() != storeId.intValue()) {
            throw new BusinessException(FORBIDDEN);
        }
    }

    private void validateAlbaAccess(LoginContext login, int storeId, int albaId) {
        validateStoreAccess(login, storeId);
        if (!albaMapper.existsAlbaById(albaId)) {
            throw new BusinessException(ALBA_NOT_FOUND);
        }
        if (albaMapper.countByStoreIdAndAlbaId(storeId, albaId) == 0) {
            throw new BusinessException(FORBIDDEN);
        }
    }

    private String normalizeOptionalEmail(String albaEmail) {
        if (!StringUtils.hasText(albaEmail)) {
            return null;
        }
        return albaEmail.trim().toLowerCase();
    }

    private Set<String> normalizeScheduleTimes(List<String> rawScheduleTimes) {
        Set<String> normalized = new LinkedHashSet<>();
        if (rawScheduleTimes == null) {
            return normalized;
        }
        for (String raw : rawScheduleTimes) {
            if (!StringUtils.hasText(raw)) {
                continue;
            }
            normalized.add(normalizeScheduleTime(raw));
        }
        return normalized;
    }

    private String normalizeScheduleTime(String rawScheduleTime) {
        String value = rawScheduleTime.trim().toUpperCase(Locale.ROOT);
        switch (value) {
            case "SUNDAY":
            case "SUN":
            case "일":
                return "SUNDAY";
            case "MONDAY":
            case "MON":
            case "월":
                return "MONDAY";
            case "TUESDAY":
            case "TUE":
            case "화":
                return "TUESDAY";
            case "WEDNESDAY":
            case "WED":
            case "수":
                return "WEDNESDAY";
            case "THURSDAY":
            case "THU":
            case "목":
                return "THURSDAY";
            case "FRIDAY":
            case "FRI":
            case "금":
                return "FRIDAY";
            case "SATURDAY":
            case "SAT":
            case "토":
                return "SATURDAY";
            default:
                throw new BusinessException(BAD_REQUEST);
        }
    }
}
