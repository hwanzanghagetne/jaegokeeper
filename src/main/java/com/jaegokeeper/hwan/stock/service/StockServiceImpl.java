package com.jaegokeeper.hwan.stock.service;

import com.jaegokeeper.hwan.exception.NotFoundException;
import com.jaegokeeper.hwan.stock.dto.StockAdjustRequestDTO;
import com.jaegokeeper.hwan.stock.dto.StockInOutRequestDTO;
import com.jaegokeeper.hwan.stock.mapper.SafeMapper;
import com.jaegokeeper.hwan.stock.mapper.StockHistoryMapper;
import com.jaegokeeper.hwan.stock.mapper.StockMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.jaegokeeper.hwan.stock.enums.StockHistoryType.*;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {

    private final StockMapper stockMapper;
    private final StockHistoryMapper stockHistoryMapper;
    private final SafeMapper safeMapper;

    @Transactional
    @Override
    public void inStock(Integer stockId, StockInOutRequestDTO dto) {

        Integer existQuantity = stockMapper.findQuantityByStockId(stockId);
        if (existQuantity == null) {
            throw new NotFoundException("존재하지 않는 재고: " + stockId);
        }

        int updated = stockMapper.increaseQuantity(stockId, dto.getAmount());
        if (updated != 1) {
            throw new IllegalStateException("재고 수정 실패");
        }
        int inserted = stockHistoryMapper.insertHistory(stockId, IN, dto.getAmount());
        if (inserted != 1) {
            throw new IllegalStateException("재고 히스토리 저장 실패");
        }
    }

    @Transactional
    @Override
    public void outStock(Integer stockId, StockInOutRequestDTO dto) {

        Integer existQuantity = stockMapper.findQuantityByStockId(stockId);
        if (existQuantity == null) {
            throw new NotFoundException("존재하지 않는 재고: " + stockId);
        }

        int updated = stockMapper.decreaseQuantity(stockId, dto.getAmount());
        if (updated != 1) {
            throw new IllegalArgumentException("재고 수정 실패");
        }
        int inserted = stockHistoryMapper.insertHistory(stockId, OUT, dto.getAmount());
        if (inserted != 1) {
            throw new IllegalStateException("재고 히스토리 저장 실패");
        }
    }

    @Transactional
    @Override
    //dto 만들어서 넘기기
    public void adjustStock(Integer stockId, StockAdjustRequestDTO dto) {

        Integer existQuantity = stockMapper.findQuantityByStockId(stockId);
        if (existQuantity == null) {
            throw new NotFoundException("존재하지 않는 재고입니다: " + stockId);
        }

        int updatedStock = stockMapper.updateQuantity(stockId, dto.getTargetQuantity(), dto.getFavoriteYn());
        if (updatedStock != 1) {
            throw new IllegalStateException("재고 수정 실패");
        }
        int updatedSafe = safeMapper.updateSafe(stockId, dto.getSafeQuantity());
        if (updatedSafe != 1) {
            throw new IllegalStateException("안전재고 수정 실패");
        }
        if ((dto.getTargetQuantity() - existQuantity) != 0) {
            int inserted = stockHistoryMapper.insertHistory(stockId, ADJUST, dto.getTargetQuantity());
            if (inserted != 1) {
                throw new IllegalStateException("재고 히스토리 저장 실패");
            }
        }
    }
}
