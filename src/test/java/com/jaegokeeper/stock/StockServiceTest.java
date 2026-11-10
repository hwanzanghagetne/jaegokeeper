package com.jaegokeeper.stock;

import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.exception.ErrorCode;
import com.jaegokeeper.stock.dto.StockAdjustRequest;
import com.jaegokeeper.stock.dto.StockAmountUpdateRequest;
import com.jaegokeeper.stock.dto.StockInOutRequest;
import com.jaegokeeper.stock.enums.LogType;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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


    @Test
    public void 입고_성공() {
        StockInOutRequest req = new StockInOutRequest();
        req.setAmount(10);

        when(stockMapper.increaseQuantity(1, 1, 10)).thenReturn(1);
        when(logMapper.insertLog(1, com.jaegokeeper.stock.enums.LogType.IN, 10)).thenReturn(1);

        stockService.inStock(login, 1, req);
    }

    @Test
    public void 입고_재고없음_예외() {
        StockInOutRequest req = new StockInOutRequest();
        req.setAmount(10);

        when(stockMapper.increaseQuantity(1, 999, 10)).thenReturn(0);

        try {
            stockService.inStock(login, 999, req);
            fail("BusinessException이 발생해야 합니다");
        } catch (BusinessException e) {
            assertEquals(ErrorCode.STOCK_NOT_FOUND, e.getErrorCode());
        }
    }


    @Test
    public void 출고_재고부족_예외() {
        StockInOutRequest req = new StockInOutRequest();
        req.setAmount(100);

        when(stockMapper.findStockAmountByItemId(1, 1)).thenReturn(10);
        when(stockMapper.decreaseQuantity(1, 1, 100)).thenReturn(0);

        try {
            stockService.outStock(login, 1, req);
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
            stockService.outStock(login, 999, req);
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

        stockService.outStock(login, 1, req);
    }

    @Test
    public void 재고조정_성공시_ADJUST_로그_기록() {
        StockAmountUpdateRequest req = new StockAmountUpdateRequest();
        req.setStockAmount(8);

        when(stockMapper.findStockAmountByItemId(1, 1)).thenReturn(5);
        when(stockMapper.updateStockAmount(1, 1, 8)).thenReturn(1);
        when(logMapper.insertLog(1, LogType.ADJUST, 8)).thenReturn(1);

        stockService.updateStockAmount(login, 1, req);

        verify(logMapper).insertLog(1, LogType.ADJUST, 8);
    }

    @Test
    public void 재고조정_수량이_같으면_갱신과_로그_없음() {
        StockAmountUpdateRequest req = new StockAmountUpdateRequest();
        req.setStockAmount(5);

        when(stockMapper.findStockAmountByItemId(1, 1)).thenReturn(5);

        stockService.updateStockAmount(login, 1, req);

        verify(stockMapper, never()).updateStockAmount(anyInt(), anyInt(), anyInt());
        verify(logMapper, never()).insertLog(anyInt(), any(), anyInt());
    }

    @Test
    public void 재고조정_재고없음_예외() {
        StockAmountUpdateRequest req = new StockAmountUpdateRequest();
        req.setStockAmount(8);

        when(stockMapper.findStockAmountByItemId(1, 1)).thenReturn(null);

        BusinessException e = assertThrows(BusinessException.class,
                () -> stockService.updateStockAmount(login, 1, req));
        assertEquals(ErrorCode.STOCK_NOT_FOUND, e.getErrorCode());
    }

    @Test
    public void 상품수정_버퍼만_변경하면_재고는_건드리지_않음() {
        when(bufferMapper.updateBufferAmount(1, 1, 4)).thenReturn(1);

        stockService.adjustStock(login, 1, new StockAdjustRequest(null, 4));

        verify(bufferMapper).updateBufferAmount(1, 1, 4);
        verify(stockMapper, never()).updateStockAmount(anyInt(), anyInt(), anyInt());
    }

    @Test
    public void 상품수정_목표수량과_버퍼를_함께_변경() {
        when(stockMapper.findStockAmountByItemId(1, 1)).thenReturn(5);
        when(stockMapper.updateStockAmount(1, 1, 9)).thenReturn(1);
        when(logMapper.insertLog(1, LogType.ADJUST, 9)).thenReturn(1);
        when(bufferMapper.updateBufferAmount(1, 1, 3)).thenReturn(1);

        stockService.adjustStock(login, 1, new StockAdjustRequest(9, 3));

        verify(logMapper).insertLog(1, LogType.ADJUST, 9);
        verify(bufferMapper).updateBufferAmount(1, 1, 3);
    }
}
