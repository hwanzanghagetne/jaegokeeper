package com.jaegokeeper.hwan.stock.service;

import com.jaegokeeper.hwan.stock.dto.StockAdjustRequest;
import com.jaegokeeper.hwan.stock.dto.StockAmountUpdateRequest;
import com.jaegokeeper.hwan.stock.dto.StockDetailResponse;
import com.jaegokeeper.hwan.stock.dto.StockInOutRequest;

public interface StockService {

    //아이템 재고 수정 (리스트)
    void inStock(Integer storeId, Integer itemId, StockInOutRequest dto);
    void outStock(Integer storeId, Integer itemId, StockInOutRequest dto);

    //아이템 재고 수정(재고만 수정/버퍼X)
    void updateStockAmount(Integer storeId, Integer itemId, StockAmountUpdateRequest dto);

    //아이템 재고 수정 (재고, 버퍼 모두 수정)
    void adjustStock(Integer itemId, Integer storeId, StockAdjustRequest dto);


    StockDetailResponse getStockDetail(Integer storeId, Integer itemId);
}
