package com.jaegokeeper.alba.service;

import com.jaegokeeper.alba.dto.AlbaDetailDto;
import com.jaegokeeper.alba.dto.AlbaListDto;
import com.jaegokeeper.alba.dto.AlbaRegisterDto;
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
    public void saveAlbaRegister(AlbaRegisterDto albaRegisterDto) {
        if (!albaMapper.existsByStoreId(albaRegisterDto.getStoreId())) {
            throw new BusinessException(STORE_NOT_FOUND);
        }
        if (albaMapper.existsByAlbaPhone(albaRegisterDto.getAlbaPhone()) > 0) {
            throw new BusinessException(BAD_REQUEST);
        }
        albaMapper.insertAlba(albaRegisterDto);
    }

    @Transactional
    public void updateAlba(AlbaDetailDto albaDetailDto) {
        albaMapper.updateAlba(albaDetailDto);
    }

    @Transactional
    public boolean deleteAlba(int albaId) {
        int deleteResult = albaMapper.deleteAlba(albaId);
        return deleteResult > 0;
    }

    public AlbaDetailDto getAlbaByStore(int storeId) {
        AlbaDetailDto store = albaMapper.getAlbaByStore(storeId);
        if (store == null) {
            throw new BusinessException(ALBA_NOT_FOUND);
        }
        return store;
    }

    public AlbaDetailDto getAlbaById(int albaId) {
        AlbaDetailDto alba = albaMapper.getAlbaById(albaId);
        if (alba == null) {
            throw new BusinessException(ALBA_NOT_FOUND);
        }
        return alba;
    }

    public List<AlbaListDto> getAllAlbaList(Integer storeId) {
        return albaMapper.selectAllAlba(storeId);
    }
}
