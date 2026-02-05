package com.jaegokeeper.hwan.stock.service;

import com.jaegokeeper.hwan.buffer.mapper.BufferMapper;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.hwan.stock.dto.StockAdjustRequest;
import com.jaegokeeper.hwan.stock.dto.StockAmountUpdateRequest;
import com.jaegokeeper.hwan.stock.dto.StockDetailResponse;
import com.jaegokeeper.hwan.stock.dto.StockInOutRequest;
import com.jaegokeeper.hwan.stock.mapper.LogMapper;
import com.jaegokeeper.hwan.stock.mapper.StockMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.jaegokeeper.exception.ErrorCode.INTERNAL_ERROR;
import static com.jaegokeeper.exception.ErrorCode.STOCK_NOT_FOUND;
import static com.jaegokeeper.hwan.stock.enums.LogType.*;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StockMapper stockMapper;
    private final LogMapper logMapper;
    private final BufferMapper bufferMapper;


    /*입고*/
    @Transactional
    @Override
    public void inStock(Integer storeId, Integer itemId, StockInOutRequest dto) {

        int updated = stockMapper.increaseQuantity(storeId,itemId, dto.getAmount());
        if (updated != 1) {
            throw new BusinessException(INTERNAL_ERROR);
        }

        int inserted = logMapper.insertLog(itemId, IN, dto.getAmount());
        if (inserted != 1) {
            throw new BusinessException(INTERNAL_ERROR);
        }
    }

    /*출고*/
    @Transactional
    @Override
    public void outStock(Integer storeId, Integer itemId, StockInOutRequest dto) {

        int updated = stockMapper.decreaseQuantity(storeId, itemId, dto.getAmount());
        if (updated != 1) {
            throw new BusinessException(INTERNAL_ERROR);
        }

        int inserted = logMapper.insertLog(itemId, OUT, dto.getAmount());
        if (inserted != 1) {
            throw new BusinessException(INTERNAL_ERROR);
        }
    }

    /*재고만 딱 조정*/
    @Override
    public void updateStockAmount(Integer storeId, Integer itemId, StockAmountUpdateRequest dto) {

        Integer existAmount = stockMapper.findStockAmountByItemId(storeId, itemId);
        if (existAmount == null) {
            throw new BusinessException(STOCK_NOT_FOUND);
        }

        if (dto.getStockAmount().equals(existAmount)) {
            return;
        }

        int updated = stockMapper.updateStockAmount(storeId,itemId, dto.getStockAmount());
        if (updated != 1) {
            throw new BusinessException(INTERNAL_ERROR);
        }

        if (!dto.getStockAmount().equals(existAmount)) {
            int inserted = logMapper.insertLog(itemId, ADJUST, dto.getStockAmount());
            if (inserted != 1) {
                throw new BusinessException(INTERNAL_ERROR);
            }
        }
    }


    /*재고, 버퍼 모두*/
    @Transactional
    @Override
    public void adjustStock(Integer itemId,Integer storeId, StockAdjustRequest dto) {

        Integer newTargetAmount = dto.getTargetAmount();
        Integer newBufferAmount = dto.getBufferAmount();

        // 둘 다 없으면 종료
        if (newBufferAmount == null && newTargetAmount == null) {
            return;
        }

        // 재고
        if (newTargetAmount != null) {
            Integer existAmount = stockMapper.findStockAmountByItemId(storeId, itemId);
            if (existAmount == null) {
                throw new BusinessException(STOCK_NOT_FOUND);
            }
            int updatedStock = stockMapper.updateStockAmount(storeId, itemId, newTargetAmount);
            if (updatedStock != 1) {
                throw new BusinessException(INTERNAL_ERROR);
            }
            if (!newTargetAmount.equals(existAmount)) {
                int inserted = logMapper.insertLog(itemId, ADJUST, newTargetAmount);
                if (inserted != 1) {
                    throw new BusinessException(INTERNAL_ERROR);
                }
            }
        }

        // 안전 재고
        if (newBufferAmount != null) {
            int updatedSafe = bufferMapper.updateBufferAmount(storeId, itemId, newBufferAmount);
            if (updatedSafe != 1) {
                throw new BusinessException(INTERNAL_ERROR);
            }
        }
    }

    /*재고 상세*/
    @Override
    public StockDetailResponse getStockDetail(Integer storeId, Integer itemId) {
        StockDetailResponse dto = stockMapper.findStockDetail(storeId, itemId);
        if (dto == null) {
            throw new BusinessException(STOCK_NOT_FOUND);
        }
        return dto;
    }
}
