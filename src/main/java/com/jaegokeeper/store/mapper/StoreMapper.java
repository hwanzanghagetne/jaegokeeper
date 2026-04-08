package com.jaegokeeper.store.mapper;

import com.jaegokeeper.store.dto.StoreDto;
import com.jaegokeeper.store.dto.StoreUpdateRequest;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StoreMapper {
    boolean existsById(int storeId);

    int insertStore(StoreDto storeDto);

    int updateStore(StoreUpdateRequest req);
}
