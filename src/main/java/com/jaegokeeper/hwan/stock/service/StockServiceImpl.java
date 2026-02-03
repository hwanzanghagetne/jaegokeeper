package com.jaegokeeper.hwan.stock.service;

import com.jaegokeeper.hwan.buffer.mapper.BufferMapper;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.hwan.stock.dto.StockAdjustRequestDTO;
import com.jaegokeeper.hwan.stock.dto.StockDetailResponse;
import com.jaegokeeper.hwan.stock.dto.StockInOutRequestDTO;
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

    @Transactional
    @Override
    public int inStock(Integer storeId, Integer stockId, StockInOutRequestDTO dto) {

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
        Integer stockAmount = stockMapper.findStockAmount(storeId, stockId);
        if (stockAmount == null) {
            throw new BusinessException(INTERNAL_ERROR);
        }
        return stockAmount;
    }

    @Transactional
    @Override
    public int outStock(Integer storeId, Integer stockId, StockInOutRequestDTO dto) {

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
        Integer stockAmount = stockMapper.findStockAmount(storeId, stockId);
        if (stockAmount == null) {
            throw new BusinessException(INTERNAL_ERROR);
        }
        return stockAmount;
    }

    @Transactional
    @Override
    //dto 만들어서 넘기기
    public void adjustStock(Integer itemId, StockAdjustRequestDTO dto) {

        Integer newTargetAmount = dto.getTargetAmount();
        Integer newBufferAmount = dto.getBufferAmount();

        // 둘 다 없으면 종료
        if (newBufferAmount == null && newTargetAmount == null) {
            return;
        }

        // 재고
        if (newTargetAmount != null) {
            Integer existAmount = stockMapper.findStockAmountByItemId(itemId);
            if (existAmount == null) {
                throw new BusinessException(STOCK_NOT_FOUND);
            }
            int updatedStock = stockMapper.updateStockAmount(itemId, newTargetAmount);
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
            int updatedSafe = bufferMapper.updateBufferAmount(itemId, newBufferAmount);
            if (updatedSafe != 1) {
                throw new BusinessException(INTERNAL_ERROR);
            }
        }
    }

    @Override
    public StockDetailResponse getStockDetail(Integer storeId, Integer stockId) {
        StockDetailResponse dto = stockMapper.findStockDetail(storeId, stockId);
        if (dto == null) {
            throw new BusinessException(STOCK_NOT_FOUND);
        }
        return dto;
    }
}
