package com.jaegokeeper.alba;

import com.jaegokeeper.alba.dto.AlbaUpdateRequest;
import com.jaegokeeper.alba.mapper.AlbaMapper;
import com.jaegokeeper.alba.mapper.WorkMapper;
import com.jaegokeeper.alba.service.AlbaService;
import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.exception.ErrorCode;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class AlbaServiceTest {

    @InjectMocks
    private AlbaService albaService;

    @Mock
    private AlbaMapper albaMapper;

    @Mock
    private WorkMapper workMapper;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void 알바수정_0건영향_예외() {
        LoginContext login = new LoginContext(10, 1, "tester", "LOCAL");
        AlbaUpdateRequest req = new AlbaUpdateRequest();
        req.setAlbaId(5);

        when(albaMapper.existsAlbaById(5)).thenReturn(true);
        when(albaMapper.countByStoreIdAndAlbaId(1, 5)).thenReturn(1);
        when(albaMapper.updateAlba(any(AlbaUpdateRequest.class))).thenReturn(0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> albaService.updateAlba(login, 1, req));

        assertEquals(ErrorCode.STATE_CONFLICT, e.getErrorCode());
    }
}
