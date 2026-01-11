package com.jaegokeeper.hwan.stock.service;

import com.jaegokeeper.hwan.buffer.mapper.BufferMapper;
import com.jaegokeeper.hwan.exception.NotFoundException;
import com.jaegokeeper.hwan.stock.dto.StockAdjustRequestDTO;
import com.jaegokeeper.hwan.stock.dto.StockInOutRequestDTO;
import com.jaegokeeper.hwan.stock.mapper.LogMapper;
import com.jaegokeeper.hwan.stock.mapper.StockMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        int updated = stockMapper.increaseQuantity(storeId,stockId, dto.getAmount());
        if (updated != 1) {
            throw new IllegalStateException("재고 수정 실패");
        }
        int inserted = logMapper.insertLog(stockId, IN, dto.getAmount());
        if (inserted != 1) {
            throw new IllegalStateException("재고 히스토리 저장 실패");
        }
    }

    @Transactional
    @Override
    public void outStock(Integer storeId, Integer stockId, StockInOutRequestDTO dto) {

        int updated = stockMapper.decreaseQuantity(storeId,stockId, dto.getAmount());
        if (updated != 1) {
            throw new IllegalStateException("재고 수정 실패");
        }
        int inserted = logMapper.insertLog(stockId, OUT, dto.getAmount());
        if (inserted != 1) {
            throw new IllegalStateException("재고 히스토리 저장 실패");
        }
    }

    @Transactional
    @Override
    //dto 만들어서 넘기기
    public void adjustStock(Integer itemId, StockAdjustRequestDTO dto) {

        Integer existAmount = stockMapper.findStockAmountByItemId(itemId);
        if (existAmount == null) {throw new NotFoundException("존재하지 않는 재고입니다: " + itemId);}

        int updatedStock = stockMapper.updateStockAmount(itemId, dto.getTargetAmount());
        if (updatedStock != 1) {throw new IllegalStateException("재고 수정 실패");}

        int updatedSafe = bufferMapper.updateBufferAmount(itemId, dto.getBufferAmount());
        if (updatedSafe != 1) {throw new IllegalStateException("안전재고 수정 실패");}


        boolean isChanged = !dto.getTargetAmount().equals(existAmount);
        if (isChanged) {
            int inserted = logMapper.insertLog(itemId, ADJUST, dto.getTargetAmount());
            if (inserted != 1) {
                throw new IllegalStateException("재고 로그 저장 실패");
            }
        }
    }
}
