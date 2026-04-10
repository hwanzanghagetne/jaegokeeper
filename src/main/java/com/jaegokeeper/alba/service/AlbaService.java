package com.jaegokeeper.alba.service;

import com.jaegokeeper.alba.dto.AlbaDetailResponse;
import com.jaegokeeper.alba.dto.AlbaListResponse;
import com.jaegokeeper.alba.dto.AlbaRegisterRequest;
import com.jaegokeeper.alba.dto.AlbaUpdateRequest;
import com.jaegokeeper.alba.mapper.AlbaMapper;
import com.jaegokeeper.alba.mapper.WorkMapper;
import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.jaegokeeper.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class AlbaService {

    private final AlbaMapper albaMapper;
    private final WorkMapper workMapper;

    @Transactional
    public void saveAlbaRegister(LoginContext login, AlbaRegisterRequest req) {
        saveAlbaRegister(login, req.getStoreId(), req);
    }

    @Transactional
    public void saveAlbaRegister(LoginContext login, int storeId, AlbaRegisterRequest req) {
        validateStoreAccess(login, storeId);
        req.setStoreId(storeId);
        if (!albaMapper.existsByStoreId(storeId)) {
            throw new BusinessException(STORE_NOT_FOUND);
        }
        if (albaMapper.existsByAlbaPhone(storeId, req.getAlbaPhone()) > 0) {
            throw new BusinessException(BAD_REQUEST);
        }
        try {
            albaMapper.insertAlba(req);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(BAD_REQUEST);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(BAD_REQUEST);
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

    public AlbaDetailResponse getAlbaByStore(int storeId) {
        AlbaDetailResponse alba = albaMapper.getAlbaByStore(storeId);
        if (alba == null) {
            throw new BusinessException(ALBA_NOT_FOUND);
        }
        return alba;
    }

    public AlbaDetailResponse getAlbaById(LoginContext login, int albaId) {
        return getAlbaById(login, login.getStoreId(), albaId);
    }

    public AlbaDetailResponse getAlbaById(LoginContext login, int storeId, int albaId) {
        validateAlbaAccess(login, storeId, albaId);
        AlbaDetailResponse alba = albaMapper.getAlbaById(albaId);
        if (alba == null) {
            throw new BusinessException(ALBA_NOT_FOUND);
        }
        return alba;
    }

    public List<AlbaListResponse> getAllAlbaList(LoginContext login, Integer storeId) {
        int targetStoreId = (storeId != null) ? storeId : login.getStoreId();
        validateStoreAccess(login, targetStoreId);
        return albaMapper.selectAllAlba(targetStoreId);
    }

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
}
