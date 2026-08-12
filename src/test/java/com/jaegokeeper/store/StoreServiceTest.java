package com.jaegokeeper.store;

import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.exception.ErrorCode;
import com.jaegokeeper.store.dto.StoreDetailResponse;
import com.jaegokeeper.store.dto.StoreUpdateRequest;
import com.jaegokeeper.store.mapper.StoreMapper;
import com.jaegokeeper.store.service.StoreService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class StoreServiceTest {

    @InjectMocks
    private StoreService storeService;

    @Mock
    private StoreMapper storeMapper;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void 점포수정_0건영향_예외() {
        LoginContext login = new LoginContext(10, 1, "tester", "LOCAL");
        StoreUpdateRequest req = new StoreUpdateRequest();

        when(storeMapper.existsById(1)).thenReturn(true);
        when(storeMapper.updateStore(any(StoreUpdateRequest.class))).thenReturn(0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> storeService.updateStore(login, req));

        assertEquals(ErrorCode.STATE_CONFLICT, e.getErrorCode());
    }

    @Test
    public void 점포조회_정상() {
        LoginContext login = new LoginContext(10, 1, "tester", "LOCAL");
        StoreDetailResponse detail = new StoreDetailResponse();
        detail.setStoreId(1);
        detail.setStoreName("테스트 매장");

        when(storeMapper.findStoreDetail(1)).thenReturn(detail);

        StoreDetailResponse result = storeService.getStoreDetail(login);

        assertEquals("테스트 매장", result.getStoreName());
    }

    // storeId가 더 이상 파라미터로 넘어오지 않으므로 "본인 매장이 아닌 storeId로 조회"
    // 시나리오는 구성할 수 없어 삭제했다 — login.getStoreId()가 유일한 출처다.

    @Test
    public void 점포조회_대상없음_예외() {
        LoginContext login = new LoginContext(10, 1, "tester", "LOCAL");

        when(storeMapper.findStoreDetail(1)).thenReturn(null);

        BusinessException e = assertThrows(BusinessException.class,
                () -> storeService.getStoreDetail(login));

        assertEquals(ErrorCode.STORE_NOT_FOUND, e.getErrorCode());
    }
}
