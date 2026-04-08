package com.jaegokeeper.store;

import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.exception.ErrorCode;
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
                () -> storeService.updateStore(login, 1, req));

        assertEquals(ErrorCode.STATE_CONFLICT, e.getErrorCode());
    }
}
