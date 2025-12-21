package com.jaegokeeper.psj.mapper;

import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface StoreMapper2 {
    boolean existsById(int StoreId);
}
