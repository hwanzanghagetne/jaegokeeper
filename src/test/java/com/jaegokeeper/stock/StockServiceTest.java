package com.jaegokeeper.stock;

import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.exception.ErrorCode;
import com.jaegokeeper.stock.dto.StockInOutRequest;
import com.jaegokeeper.stock.mapper.BufferMapper;
import com.jaegokeeper.stock.mapper.LogMapper;
import com.jaegokeeper.stock.mapper.StockMapper;
import com.jaegokeeper.stock.service.StockService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class StockServiceTest {

    @InjectMocks
    private StockService stockService;

    @Mock
    private StockMapper stockMapper;

    @Mock
    private LogMapper logMapper;

    @Mock
    private BufferMapper bufferMapper;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ===================== inStock =====================

    @Test
    public void 입고_성공() {
        StockInOutRequest req = new StockInOutRequest();
        req.setAmount(10);

        when(stockMapper.increaseQuantity(1, 1, 10)).thenReturn(1);
        when(logMapper.insertLog(1, com.jaegokeeper.stock.enums.LogType.IN, 10)).thenReturn(1);

        stockService.inStock(1, 1, req); // 예외 없으면 성공
    }

    @Test
    public void 입고_재고없음_예외() {
        StockInOutRequest req = new StockInOutRequest();
        req.setAmount(10);

        when(stockMapper.increaseQuantity(1, 999, 10)).thenReturn(0);

        try {
            stockService.inStock(1, 999, req);
        } catch (BusinessException e) {
            assertEquals(ErrorCode.STOCK_NOT_FOUND, e.getErrorCode());
        }
    }

    // ===================== outStock =====================

    @Test
    public void 출고_재고부족_예외() {
        StockInOutRequest req = new StockInOutRequest();
        req.setAmount(100);

        when(stockMapper.findStockAmountByItemId(1, 1)).thenReturn(10);

        try {
            stockService.outStock(1, 1, req);
        } catch (BusinessException e) {
            assertEquals(ErrorCode.STOCK_QUANTITY_NOT_ENOUGH, e.getErrorCode());
        }
    }

    @Test
    public void 출고_재고없음_예외() {
        StockInOutRequest req = new StockInOutRequest();
        req.setAmount(10);

        when(stockMapper.findStockAmountByItemId(1, 999)).thenReturn(null);

        try {
            stockService.outStock(1, 999, req);
        } catch (BusinessException e) {
            assertEquals(ErrorCode.STOCK_NOT_FOUND, e.getErrorCode());
        }
    }

    @Test
    public void 출고_성공() {
        StockInOutRequest req = new StockInOutRequest();
        req.setAmount(5);

        when(stockMapper.findStockAmountByItemId(1, 1)).thenReturn(10);
        when(stockMapper.decreaseQuantity(1, 1, 5)).thenReturn(1);
        when(logMapper.insertLog(1, com.jaegokeeper.stock.enums.LogType.OUT, 5)).thenReturn(1);

        stockService.outStock(1, 1, req); // 예외 없으면 성공
    }
}
