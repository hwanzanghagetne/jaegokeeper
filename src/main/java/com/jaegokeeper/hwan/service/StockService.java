package com.jaegokeeper.hwan.service;

import com.jaegokeeper.hwan.dto.ItemModifyRequestDTO;
import com.jaegokeeper.hwan.dto.StockInOutRequestDTO;

public interface StockService {

    //아이템 재고 수정 (리스트)
    void inStock(Integer stockId, StockInOutRequestDTO dto);
    void outStock(Integer stockId, StockInOutRequestDTO dto);

    //아이템 재고 수정 (상세)
    void adjustStock(Integer stockId, Integer targetQuantity, Integer safeQuantity, Boolean favoriteYn);

}
