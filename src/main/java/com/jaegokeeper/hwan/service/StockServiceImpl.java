package com.jaegokeeper.hwan.service;

import com.jaegokeeper.hwan.domain.enums.StockHistoryType;
import com.jaegokeeper.hwan.dto.StockInOutRequestDTO;
import com.jaegokeeper.hwan.mapper.SafeMapper;
import com.jaegokeeper.hwan.mapper.StockHistoryMapper;
import com.jaegokeeper.hwan.mapper.StockMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.jaegokeeper.hwan.domain.enums.StockHistoryType.*;

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

        Integer delta = -dto.getAmount();//음수 넣기 위해

        int updated = stockMapper.decreaseQuantity(stockId, dto.getAmount());
        if (updated != 1) {
            throw new IllegalArgumentException("재고 부족");
        }
        stockHistoryMapper.insertHistory(stockId, OUT, delta);
    }

    @Transactional
    @Override
    public void adjustStock(Integer stockId, Integer targetQuantity, Integer safeQuantity, Boolean favoriteYn) {
        Integer existQuantity = stockMapper.findQuantityByStockId(stockId);
        int delta = targetQuantity - existQuantity;
        int updatedStock = stockMapper.updateQuantity(stockId, targetQuantity, favoriteYn);
        if (updatedStock != 1) {
            throw new IllegalStateException("재고 수정 실패");
        }
        int updatedSafe = safeMapper.updateSafe(stockId, safeQuantity);
        if (updatedSafe != 1) {
            throw new IllegalStateException("안전재고 수정 실패");
        }
        if (delta != 0) {
            int inserted = stockHistoryMapper.insertHistory(stockId, ADJUST, delta);
            if (inserted != 1) {
                throw new IllegalStateException("재고 히스토리 저장 실패");
            }
        }
    }
}
