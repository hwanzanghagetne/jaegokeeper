package com.jaegokeeper.alba.service;

import com.jaegokeeper.alba.dto.AlbaDetailDto;
import com.jaegokeeper.alba.dto.AlbaListDto;
import com.jaegokeeper.alba.dto.AlbaRegisterDto;
import com.jaegokeeper.alba.mapper.AlbaMapper;
import com.jaegokeeper.alba.mapper.WorkMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AlbaService {

    private final AlbaMapper albaMapper;
    private final WorkMapper workMapper;
    public AlbaService(AlbaMapper albaMapper, WorkMapper workMapper) {
        this.albaMapper = albaMapper;
        this.workMapper = workMapper;
    }

    @Transactional
    public void saveAlbaRegister(AlbaRegisterDto albaRegisterDto) {
        if (!albaMapper.existsByStoreId(albaRegisterDto.getStoreId())) {
            throw new IllegalArgumentException("Store가 존재하지 않습니다.");
        }
        if (albaMapper.existsByAlbaPhone(albaRegisterDto.getAlbaPhone()) > 0) {
            throw new IllegalArgumentException("존재하는 알바 전화번호 입니다.");
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
            throw new IllegalArgumentException("해당 알바생이 존재하지 않습니다.");
        }
        return store;
    }

    public AlbaDetailDto getAlbaById(int albaId) {
        AlbaDetailDto alba = albaMapper.getAlbaById(albaId);
        if (alba == null) {
            throw new IllegalArgumentException("해당 알바생이 존재하지 않습니다.");
        }
        return alba;
    }

    public List<AlbaListDto> getAllAlbaList(Integer storeId) {
        return albaMapper.selectAllAlba(storeId);
    }
}
