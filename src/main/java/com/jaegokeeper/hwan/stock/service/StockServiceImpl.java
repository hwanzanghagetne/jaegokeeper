package com.jaegokeeper.hwan.stock.service;

import com.jaegokeeper.hwan.buffer.mapper.BufferMapper;
import com.jaegokeeper.hwan.exception.BusinessException;
import com.jaegokeeper.hwan.stock.dto.StockAdjustRequestDTO;
import com.jaegokeeper.hwan.stock.dto.StockInOutRequestDTO;
import com.jaegokeeper.hwan.stock.mapper.LogMapper;
import com.jaegokeeper.hwan.stock.mapper.StockMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.jaegokeeper.hwan.exception.ErrorCode.*;
import static com.jaegokeeper.hwan.stock.enums.LogType.*;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StockMapper stockMapper;
    private final LogMapper logMapper;
    private final BufferMapper bufferMapper;

    @Transactional
    @Override
    public void inStock(Integer storeId, Integer stockId, StockInOutRequestDTO dto) {

        Integer itemId = stockMapper.findItemIdByStockId(stockId,storeId);
        if (itemId == null) {
            throw new BusinessException(STOCK_NOT_FOUND);
        }

        int updated = stockMapper.increaseQuantity(storeId,stockId, dto.getAmount());
        if (updated != 1) {
            throw new BusinessException(INTERNAL_ERROR);
        }

        int inserted = logMapper.insertLog(itemId, IN, dto.getAmount());
        if (inserted != 1) {
            throw new BusinessException(INTERNAL_ERROR);
        }
    }

    @Transactional
    @Override
    public void outStock(Integer storeId, Integer stockId, StockInOutRequestDTO dto) {

        Integer itemId = stockMapper.findItemIdByStockId(stockId,storeId);
        if (itemId == null) {
            throw new BusinessException(STOCK_NOT_FOUND);
        }

        int updated = stockMapper.decreaseQuantity(storeId,stockId, dto.getAmount());
        if (updated != 1) {
            throw new BusinessException(INTERNAL_ERROR);
        }

        int inserted = logMapper.insertLog(itemId, OUT, dto.getAmount());
        if (inserted != 1) {
            throw new BusinessException(INTERNAL_ERROR);
        }
    }

    @Transactional
    @Override
    //dto 만들어서 넘기기
    public void adjustStock(Integer itemId, StockAdjustRequestDTO dto) {

        Integer existAmount = stockMapper.findStockAmountByItemId(itemId);
        if (existAmount == null) {
            throw new BusinessException(STOCK_NOT_FOUND);
        }

        int updatedStock = stockMapper.updateStockAmount(itemId, dto.getTargetAmount());
        if (updatedStock != 1) {
            throw new BusinessException(INTERNAL_ERROR);
        }

        int updatedSafe = bufferMapper.updateBufferAmount(itemId, dto.getBufferAmount());
        if (updatedSafe != 1) {
            throw new BusinessException(INTERNAL_ERROR);
        }

        boolean isChanged = !dto.getTargetAmount().equals(existAmount);
        if (isChanged) {
            int inserted = logMapper.insertLog(itemId, ADJUST, dto.getTargetAmount());
            if (inserted != 1) {
                throw new BusinessException(INTERNAL_ERROR);
            }
        }
    }
}
