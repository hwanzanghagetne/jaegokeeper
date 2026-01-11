package com.jaegokeeper.psj.mapper;

import com.jaegokeeper.psj.dto.StoreDto;
import com.jaegokeeper.psj.dto.UserDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StoreMapper2 {
    boolean existsById(int StoreId);

    // store 정보 수정
    void updateStore(StoreDto storeDto);
}
