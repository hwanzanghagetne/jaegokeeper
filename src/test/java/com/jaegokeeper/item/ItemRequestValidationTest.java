package com.jaegokeeper.item;

import com.jaegokeeper.item.dto.request.ItemCreateRequest;
import com.jaegokeeper.item.dto.request.ItemUpdateRequest;
import com.jaegokeeper.stock.dto.StockInOutRequest;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ItemRequestValidationTest {

    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    public void 상품명이_50자를_넘으면_생성_검증_실패() {
        ItemCreateRequest req = new ItemCreateRequest();
        req.setItemName("a".repeat(51));
        req.setStockAmount(1);

        assertEquals(1, validator.validate(req).size());
    }

    @Test
    public void 상품명_50자와_수량_상한은_통과() {
        ItemCreateRequest req = new ItemCreateRequest();
        req.setItemName("a".repeat(50));
        req.setStockAmount(1_000_000);

        assertTrue(validator.validate(req).isEmpty());
    }

    @Test
    public void 수량이_상한을_넘으면_생성_검증_실패() {
        ItemCreateRequest req = new ItemCreateRequest();
        req.setItemName("item");
        req.setStockAmount(1_000_001);

        assertEquals(1, validator.validate(req).size());
    }

    @Test
    public void 수정_시_빈_상품명은_검증_실패_null은_통과() {
        ItemUpdateRequest empty = new ItemUpdateRequest();
        empty.setItemName("");
        assertEquals(1, validator.validate(empty).size());

        assertTrue(validator.validate(new ItemUpdateRequest()).isEmpty());
    }

    @Test
    public void 수정_시_목표수량과_버퍼가_상한을_넘으면_검증_실패() {
        ItemUpdateRequest req = new ItemUpdateRequest();
        req.setTargetAmount(1_000_001);
        req.setBufferAmount(1_000_001);

        assertEquals(2, validator.validate(req).size());
    }

    @Test
    public void 입출고_수량이_상한을_넘으면_검증_실패() {
        StockInOutRequest req = new StockInOutRequest();
        ReflectionTestUtils.setField(req, "amount", 1_000_001);

        assertEquals(1, validator.validate(req).size());
    }
}
