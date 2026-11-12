package com.jaegokeeper.stock.service;

import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.stock.dto.StockAndBufferUpdate;
import com.jaegokeeper.stock.dto.StockAmountUpdateRequest;
import com.jaegokeeper.stock.dto.StockDetailResponse;
import com.jaegokeeper.stock.dto.StockInOutRequest;
import com.jaegokeeper.stock.mapper.BufferMapper;
import com.jaegokeeper.stock.mapper.LogMapper;
import com.jaegokeeper.stock.mapper.StockMapper;
import com.jaegokeeper.stock.model.Stock;
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
    public void inStock(LoginContext login, Integer itemId, StockInOutRequest dto) {
        int storeId = login.getStoreId();
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
    public void outStock(LoginContext login, Integer itemId, StockInOutRequest dto) {
        int storeId = login.getStoreId();
        findStockAmountOrThrow(storeId, itemId);
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
    public void adjustStockAmount(LoginContext login, Integer itemId, StockAmountUpdateRequest dto) {
        changeStockAmount(login.getStoreId(), itemId, dto.getStockAmount());
    }

    @Transactional
    public void updateStockAndBuffer(LoginContext login, Integer itemId, StockAndBufferUpdate dto) {
        int storeId = login.getStoreId();

        if (dto.getTargetAmount() != null) {
            changeStockAmount(storeId, itemId, dto.getTargetAmount());
        }

        if (dto.getBufferAmount() != null) {
            int updatedBuffer = bufferMapper.updateBufferAmount(storeId, itemId, dto.getBufferAmount());
            if (updatedBuffer != 1) {
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

    @Transactional(readOnly = true)
    public StockDetailResponse getStockDetail(LoginContext login, Integer itemId) {
        StockDetailResponse dto = stockMapper.findStockDetail(login.getStoreId(), itemId);
        if (dto == null) {
            throw new BusinessException(STOCK_NOT_FOUND);
        }
        return dto;
    }

    private void changeStockAmount(int storeId, Integer itemId, Integer newAmount) {
        int existAmount = findStockAmountOrThrow(storeId, itemId);
        if (newAmount.equals(existAmount)) {
            return;
        }
        int updated = stockMapper.updateStockAmount(storeId, itemId, newAmount);
        if (updated != 1) {
            throw new BusinessException(STOCK_NOT_FOUND);
        }
        int inserted = logMapper.insertLog(itemId, ADJUST, newAmount);
        if (inserted != 1) {
            throw new BusinessException(INTERNAL_ERROR);
        }
    }

    private int findStockAmountOrThrow(Integer storeId, Integer itemId) {
        Integer amount = stockMapper.findStockAmountByItemId(storeId, itemId);
        if (amount == null) {
            throw new BusinessException(STOCK_NOT_FOUND);
        }
        return amount;
    }
}
