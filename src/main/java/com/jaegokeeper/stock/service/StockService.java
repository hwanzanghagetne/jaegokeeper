package com.jaegokeeper.stock.service;

import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.stock.dto.StockAdjustRequest;
import com.jaegokeeper.stock.dto.StockAmountUpdateRequest;
import com.jaegokeeper.stock.dto.StockDetailResponse;
import com.jaegokeeper.stock.dto.StockInOutRequest;
import com.jaegokeeper.stock.mapper.BufferMapper;
import com.jaegokeeper.stock.mapper.LogMapper;
import com.jaegokeeper.stock.mapper.StockMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.jaegokeeper.exception.ErrorCode.*;
import static com.jaegokeeper.stock.enums.LogType.*;

@Service
@RequiredArgsConstructor
public class StockService {

    private final StockMapper stockMapper;
    private final LogMapper logMapper;
    private final BufferMapper bufferMapper;

    @Transactional
    public void inStock(Integer storeId, Integer itemId, StockInOutRequest dto) {
        int updated = stockMapper.increaseQuantity(storeId, itemId, dto.getAmount());
        if (updated != 1) {
            throw new BusinessException(STOCK_NOT_FOUND);
        }
        int inserted = logMapper.insertLog(itemId, IN, dto.getAmount());
        if (inserted != 1) {
            throw new BusinessException(INTERNAL_ERROR);
        }
    }

    @Transactional
    public void outStock(Integer storeId, Integer itemId, StockInOutRequest dto) {
        Integer existAmount = stockMapper.findStockAmountByItemId(storeId, itemId);
        if (existAmount == null) {
            throw new BusinessException(STOCK_NOT_FOUND);
        }
        if (existAmount < dto.getAmount()) {
            throw new BusinessException(STOCK_QUANTITY_NOT_ENOUGH);
        }
        int updated = stockMapper.decreaseQuantity(storeId, itemId, dto.getAmount());
        if (updated != 1) {
            throw new BusinessException(STOCK_QUANTITY_NOT_ENOUGH);
        }
        int inserted = logMapper.insertLog(itemId, OUT, dto.getAmount());
        if (inserted != 1) {
            throw new BusinessException(INTERNAL_ERROR);
        }
    }

    @Transactional
    public void updateStockAmount(Integer storeId, Integer itemId, StockAmountUpdateRequest dto) {
        Integer existAmount = stockMapper.findStockAmountByItemId(storeId, itemId);
        if (existAmount == null) {
            throw new BusinessException(STOCK_NOT_FOUND);
        }
        if (dto.getStockAmount().equals(existAmount)) {
            return;
        }
        int updated = stockMapper.updateStockAmount(storeId, itemId, dto.getStockAmount());
        if (updated != 1) {
            throw new BusinessException(STOCK_NOT_FOUND);
        }
        int inserted = logMapper.insertLog(itemId, ADJUST, dto.getStockAmount());
        if (inserted != 1) {
            throw new BusinessException(INTERNAL_ERROR);
        }
    }

    @Transactional
    public void adjustStock(Integer storeId, Integer itemId, StockAdjustRequest dto) {
        Integer newTargetAmount = dto.getTargetAmount();
        Integer newBufferAmount = dto.getBufferAmount();

        if (newBufferAmount == null && newTargetAmount == null) {
            return;
        }

        if (newTargetAmount != null) {
            Integer existAmount = stockMapper.findStockAmountByItemId(storeId, itemId);
            if (existAmount == null) {
                throw new BusinessException(STOCK_NOT_FOUND);
            }
            int updatedStock = stockMapper.updateStockAmount(storeId, itemId, newTargetAmount);
            if (updatedStock != 1) {
                throw new BusinessException(STOCK_NOT_FOUND);
            }
            if (!newTargetAmount.equals(existAmount)) {
                int inserted = logMapper.insertLog(itemId, ADJUST, newTargetAmount);
                if (inserted != 1) {
                    throw new BusinessException(INTERNAL_ERROR);
                }
            }
        }

        if (newBufferAmount != null) {
            int updatedSafe = bufferMapper.updateBufferAmount(storeId, itemId, newBufferAmount);
            if (updatedSafe != 1) {
                throw new BusinessException(STOCK_NOT_FOUND);
            }
        }
    }

    @Transactional
    public void initStock(Integer itemId, Integer stockAmount) {
        Stock stock = Stock.create(itemId, stockAmount);
        int insertedStock = stockMapper.insertStock(stock);
        if (insertedStock != 1) {
            throw new BusinessException(INTERNAL_ERROR);
        }
        int insertedBuffer = bufferMapper.insertBuffer(itemId, 0);
        if (insertedBuffer != 1) {
            throw new BusinessException(INTERNAL_ERROR);
        }
    }

    public StockDetailResponse getStockDetail(Integer storeId, Integer itemId) {
        StockDetailResponse dto = stockMapper.findStockDetail(storeId, itemId);
        if (dto == null) {
            throw new BusinessException(STOCK_NOT_FOUND);
        }
        return dto;
    }
}
