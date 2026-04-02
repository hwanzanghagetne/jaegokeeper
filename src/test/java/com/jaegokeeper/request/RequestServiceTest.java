package com.jaegokeeper.request;

import com.jaegokeeper.alba.mapper.AlbaMapper;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.exception.ErrorCode;
import com.jaegokeeper.item.mapper.ItemMapper;
import com.jaegokeeper.request.dto.request.RequestStatusUpdateRequest;
import com.jaegokeeper.request.enums.RequestStatus;
import com.jaegokeeper.request.mapper.RequestMapper;
import com.jaegokeeper.request.service.RequestService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class RequestServiceTest {

    @InjectMocks
    private RequestService requestService;

    @Mock
    private RequestMapper requestMapper;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private AlbaMapper albaMapper;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // ===================== updateRequestStatus =====================

    @Test
    public void 요청상태변경_존재하지않는요청_예외() {
        when(requestMapper.findRequestStatus(1, 999)).thenReturn(null);

        RequestStatusUpdateRequest req = new RequestStatusUpdateRequest();
        req.setRequestStatus(RequestStatus.CONFIRM);

        try {
            requestService.updateRequestStatus(1, 999, req);
        } catch (BusinessException e) {
            assertEquals(ErrorCode.REQUEST_NOT_FOUND, e.getErrorCode());
        }
    }

    @Test
    public void 요청상태변경_상태전이불가_예외() {
        when(requestMapper.findRequestStatus(1, 1)).thenReturn(RequestStatus.CONFIRM);
        when(requestMapper.updateRequestStatus(1, 1, RequestStatus.CONFIRM)).thenReturn(0);

        RequestStatusUpdateRequest req = new RequestStatusUpdateRequest();
        req.setRequestStatus(RequestStatus.CONFIRM);

        try {
            requestService.updateRequestStatus(1, 1, req);
        } catch (BusinessException e) {
            assertEquals(ErrorCode.STATE_CONFLICT, e.getErrorCode());
        }
    }

    @Test
    public void 요청상태변경_성공() {
        when(requestMapper.findRequestStatus(1, 1)).thenReturn(RequestStatus.WAIT);
        when(requestMapper.updateRequestStatus(1, 1, RequestStatus.CONFIRM)).thenReturn(1);

        RequestStatusUpdateRequest req = new RequestStatusUpdateRequest();
        req.setRequestStatus(RequestStatus.CONFIRM);

        requestService.updateRequestStatus(1, 1, req); // 예외 없으면 성공
    }

    // ===================== getRequestDetail =====================

    @Test
    public void 요청상세_존재하지않는요청_예외() {
        when(requestMapper.findRequestDetail(1, 999)).thenReturn(null);

        try {
            requestService.getRequestDetail(1, 999);
        } catch (BusinessException e) {
            assertEquals(ErrorCode.REQUEST_NOT_FOUND, e.getErrorCode());
        }
    }
}
