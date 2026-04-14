package com.jaegokeeper.stock;

import com.jaegokeeper.auth.dto.LoginContext;
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

import static org.junit.Assert.*;
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

    private LoginContext login;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        login = new LoginContext(100, 1, "tester", "LOCAL");
    }

    // ===================== inStock =====================

    @Test
    public void 입고_성공() {
        StockInOutRequest req = new StockInOutRequest();
        req.setAmount(10);

        when(stockMapper.increaseQuantity(1, 1, 10)).thenReturn(1);
        when(logMapper.insertLog(1, com.jaegokeeper.stock.enums.LogType.IN, 10)).thenReturn(1);

        stockService.inStock(login, 1, 1, req);
    }

    @Test
    public void 입고_재고없음_예외() {
        StockInOutRequest req = new StockInOutRequest();
        req.setAmount(10);

        when(stockMapper.increaseQuantity(1, 999, 10)).thenReturn(0);

        try {
            stockService.inStock(login, 1, 999, req);
            fail("BusinessException이 발생해야 합니다");
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
        when(stockMapper.decreaseQuantity(1, 1, 100)).thenReturn(0);

        try {
            stockService.outStock(login, 1, 1, req);
            fail("BusinessException이 발생해야 합니다");
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
            stockService.outStock(login, 1, 999, req);
            fail("BusinessException이 발생해야 합니다");
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

        stockService.outStock(login, 1, 1, req);
    }
}
