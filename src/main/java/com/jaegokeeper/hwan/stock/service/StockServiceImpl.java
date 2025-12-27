package com.jaegokeeper.hwan.stock.service;

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

        int updated = stockMapper.increaseQuantity(stockId, dto.getAmount());
        if (updated != 1) {
            throw new IllegalArgumentException("재고 수정에 실패");
        }
        stockHistoryMapper.insertHistory(stockId, IN, dto.getAmount());
    }

    @Transactional
    @Override
    public void outStock(Integer stockId, StockInOutRequestDTO dto) {

        int updated = stockMapper.decreaseQuantity(stockId, dto.getAmount());
        if (updated != 1) {
            throw new IllegalArgumentException("재고 부족");
        }
        stockHistoryMapper.insertHistory(stockId, OUT, dto.getAmount());
    }

    @Transactional
    @Override
    public void adjustStock(Integer stockId, Integer targetQuantity, Integer safeQuantity, Boolean favoriteYn) {

        Integer existQuantity = stockMapper.findQuantityByStockId(stockId);
        if (existQuantity == null) {
            throw new IllegalArgumentException("존재하지 않는 재고입니다: " + stockId);
        }
        if (targetQuantity == null || targetQuantity < 0) {
            throw new IllegalArgumentException("재고 수정 실패");
        }
        int updatedStock = stockMapper.updateQuantity(stockId, targetQuantity, favoriteYn);
        if (updatedStock != 1) {
            throw new IllegalStateException("재고 수정 실패");
        }
        int updatedSafe = safeMapper.updateSafe(stockId, safeQuantity);
        if (updatedSafe != 1) {
            throw new IllegalStateException("안전재고 수정 실패");
        }
        if ((targetQuantity - existQuantity) != 0) {
            int inserted = stockHistoryMapper.insertHistory(stockId, ADJUST, targetQuantity);
            if (inserted != 1) {
                throw new IllegalStateException("재고 히스토리 저장 실패");
            }
        }
    }
}
