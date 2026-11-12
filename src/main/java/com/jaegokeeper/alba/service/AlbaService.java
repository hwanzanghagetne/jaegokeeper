package com.jaegokeeper.alba.service;

import com.jaegokeeper.alba.dto.AlbaDetailResponse;
import com.jaegokeeper.alba.dto.AlbaListResponse;
import com.jaegokeeper.alba.dto.AlbaRegisterRequest;
import com.jaegokeeper.alba.dto.AlbaUpdateRequest;
import com.jaegokeeper.alba.mapper.AlbaMapper;
import com.jaegokeeper.alba.model.Alba;
import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.mail.MailService;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.image.dto.ImageInfoDTO;
import com.jaegokeeper.image.service.ImageService;
import com.jaegokeeper.schedule.ScheduleTimes;
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
import java.util.Set;

import static com.jaegokeeper.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class AlbaService {

    private final AlbaMapper albaMapper;
    private final ScheduleMapper scheduleMapper;
    private final ImageService imageService;
    private final MailService mailService;

    @Transactional
    public int saveAlbaRegister(LoginContext login, AlbaRegisterRequest req) {
        int storeId = login.getStoreId();
        String albaEmail = normalizeOptionalEmail(req.getAlbaEmail());
        if (!albaMapper.existsByStoreId(storeId)) {
            throw new BusinessException(STORE_NOT_FOUND);
        }
        if (albaMapper.existsByAlbaPhone(storeId, req.getAlbaPhone(), null) > 0) {
            throw new BusinessException(ALBA_PHONE_DUPLICATE);
        }
        if (albaEmail != null && albaMapper.existsByAlbaEmail(storeId, albaEmail, null) > 0) {
            throw new BusinessException(ALBA_EMAIL_DUPLICATE);
        }
        ImageInfoDTO uploadedImage = null;
        try {
            uploadedImage = imageService.uploadIfPresent(req.getFile());
            Integer imageId = (uploadedImage != null) ? uploadedImage.getImageId() : null;

            Alba alba = Alba.create(storeId, req.getAlbaName(), req.getAlbaPhone(),
                    albaEmail, req.getAlbaStatus(), imageId);
            try {
                albaMapper.insertAlba(alba);
            } catch (DuplicateKeyException e) {
                boolean phoneTaken = albaMapper.existsByAlbaPhone(storeId, req.getAlbaPhone(), null) > 0;
                throw new BusinessException(phoneTaken ? ALBA_PHONE_DUPLICATE : ALBA_EMAIL_DUPLICATE);
            }
            if (alba.getAlbaId() == null) {
                throw new BusinessException(INTERNAL_ERROR);
            }

            Set<String> scheduleTimes = normalizeScheduleTimes(req.getScheduleTimes());
            for (String scheduleTime : scheduleTimes) {
                ScheduleRegisterRequest scheduleReq = new ScheduleRegisterRequest();
                scheduleReq.setAlbaId(alba.getAlbaId());
                scheduleReq.setScheduleTime(scheduleTime);
                scheduleMapper.insertSchedule(scheduleReq);
            }

            if (albaEmail != null) {
                mailService.sendWelcome(albaEmail, alba.getAlbaName());
            }

            return alba.getAlbaId();
        } catch (DuplicateKeyException e) {
            if (uploadedImage != null) imageService.deleteImageFile(uploadedImage.getImagePath());
            throw new BusinessException(SCHEDULE_TIME_DUPLICATE);
        } catch (DataIntegrityViolationException e) {
            if (uploadedImage != null) imageService.deleteImageFile(uploadedImage.getImagePath());
            throw new BusinessException(BAD_REQUEST);
        } catch (BusinessException e) {
            if (uploadedImage != null) imageService.deleteImageFile(uploadedImage.getImagePath());
            throw e;
        }
    }

    @Transactional
    public void updateAlba(LoginContext login, AlbaUpdateRequest req) {
        int storeId = login.getStoreId();
        req.setAlbaEmail(normalizeOptionalEmail(req.getAlbaEmail()));
        if (albaMapper.countByStoreIdAndAlbaId(storeId, req.getAlbaId()) == 0) {
            throw new BusinessException(ALBA_NOT_FOUND);
        }
        if (req.getAlbaPhone() != null
                && albaMapper.existsByAlbaPhone(storeId, req.getAlbaPhone(), req.getAlbaId()) > 0) {
            throw new BusinessException(ALBA_PHONE_DUPLICATE);
        }
        if (req.getAlbaEmail() != null
                && albaMapper.existsByAlbaEmail(storeId, req.getAlbaEmail(), req.getAlbaId()) > 0) {
            throw new BusinessException(ALBA_EMAIL_DUPLICATE);
        }
        int updated = albaMapper.updateAlba(storeId, req);
        if (updated == 0) {
            throw new BusinessException(STATE_CONFLICT);
        }
    }

    @Transactional
    public void deleteAlba(LoginContext login, int albaId) {
        if (albaMapper.deleteAlba(login.getStoreId(), albaId) == 0) {
            throw new BusinessException(ALBA_NOT_FOUND);
        }
    }

    @Transactional(readOnly = true)
    public AlbaDetailResponse getAlbaById(LoginContext login, int albaId) {
        AlbaDetailResponse alba = albaMapper.getAlbaById(login.getStoreId(), albaId);
        if (alba == null) {
            throw new BusinessException(ALBA_NOT_FOUND);
        }
        return alba;
    }

    @Transactional(readOnly = true)
    public List<AlbaListResponse> getAllAlbaList(LoginContext login) {
        return albaMapper.selectAllAlba(login.getStoreId());
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
            normalized.add(ScheduleTimes.normalize(raw));
        }
        return normalized;
    }
}
