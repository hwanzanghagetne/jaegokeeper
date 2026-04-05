package com.jaegokeeper.alba.service;

import com.jaegokeeper.alba.dto.AlbaDetailResponse;
import com.jaegokeeper.alba.dto.AlbaListResponse;
import com.jaegokeeper.alba.dto.AlbaRegisterRequest;
import com.jaegokeeper.alba.dto.AlbaUpdateRequest;
import com.jaegokeeper.alba.mapper.AlbaMapper;
import com.jaegokeeper.alba.mapper.WorkMapper;
import com.jaegokeeper.exception.BusinessException;
import lombok.RequiredArgsConstructor;
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
    public void saveAlbaRegister(AlbaRegisterRequest req) {
        if (!albaMapper.existsByStoreId(req.getStoreId())) {
            throw new BusinessException(STORE_NOT_FOUND);
        }
        if (albaMapper.existsByAlbaPhone(req.getAlbaPhone()) > 0) {
            throw new BusinessException(BAD_REQUEST);
        }
        albaMapper.insertAlba(req);
    }

    @Transactional
    public void updateAlba(AlbaUpdateRequest req) {
        albaMapper.updateAlba(req);
    }

    @Transactional
    public boolean deleteAlba(int albaId) {
        int deleteResult = albaMapper.deleteAlba(albaId);
        return deleteResult > 0;
    }

    public AlbaDetailResponse getAlbaByStore(int storeId) {
        AlbaDetailResponse alba = albaMapper.getAlbaByStore(storeId);
        if (alba == null) {
            throw new BusinessException(ALBA_NOT_FOUND);
        }
        return alba;
    }

    public AlbaDetailResponse getAlbaById(int albaId) {
        AlbaDetailResponse alba = albaMapper.getAlbaById(albaId);
        if (alba == null) {
            throw new BusinessException(ALBA_NOT_FOUND);
        }
        return alba;
    }

    public List<AlbaListResponse> getAllAlbaList(Integer storeId) {
        return albaMapper.selectAllAlba(storeId);
    }
}
