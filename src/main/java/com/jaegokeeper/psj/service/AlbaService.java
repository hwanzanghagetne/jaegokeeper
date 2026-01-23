package com.jaegokeeper.psj.service;

import com.jaegokeeper.psj.dto.AlbaDetailDto;
import com.jaegokeeper.psj.dto.AlbaListDto;
import com.jaegokeeper.psj.dto.AlbaRegisterDto;
import com.jaegokeeper.psj.mapper.AlbaMapper;
import com.jaegokeeper.psj.mapper.WorkMapper;
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
    
    // 알바생 등록 페이지
    @Transactional
    public void saveAlbaRegister(AlbaRegisterDto albaRegisterDto) {
        // store 존재 확인
        if(!albaMapper.existsByStoreId(albaRegisterDto.getStoreId())) {
            throw new IllegalArgumentException("Store가 존재하지 않습니다.");
        }
        // alba 전화번호 중복 체크
        if(albaMapper.existsByAlbaPhone(albaRegisterDto.getAlbaPhone()) > 0) {
            throw new IllegalArgumentException("존재하는 알바 전화번호 입니다.");
        }

        // alba 저장
        albaMapper.insertAlba(albaRegisterDto);

        // alba_id 생성되었는지 확인
//        if(albaRegisterDto.getAlbaId() == null) {
//            throw new IllegalStateException("Alba ID가 생성되지 않았습니다.");
//        }
//        workMapper.insertWork(albaRegisterDto);

    }

    // 알바생 수정 페이지
    @Transactional
    public void updateAlba(AlbaDetailDto albaDetailDto) {
        albaMapper.updateAlba(albaDetailDto);
    }

    // 알바생 삭제 페이지
    @Transactional
    public boolean deleteAlba(int albaId) {
        int deleteResult = albaMapper.deleteAlba(albaId);
        return deleteResult > 0;
    }

    // 알바생 스토어 별로 조회하는 페이지
    public AlbaDetailDto getAlbaByStore(int storeId) {
        AlbaDetailDto store = albaMapper.getAlbaByStore(storeId);

        if (store == null) {
            throw new IllegalArgumentException("해당 알바생이 존재하지 않습니다.");
        }
        return store;
    }

    // 알바생 상세 페이지
    public AlbaDetailDto getAlbaById(int albaId) {
        AlbaDetailDto alba = albaMapper.getAlbaById(albaId);

        if (alba == null) {
            throw new IllegalArgumentException("해당 알바생이 존재하지 않습니다.");
        }
        return alba;

    }

    // 알바생 관리 페이지
    public List<AlbaListDto> getAllAlbaList(Integer storeId) {
        return albaMapper.selectAllAlba(storeId);
    }
}
