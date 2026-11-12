package com.jaegokeeper.request;

import com.jaegokeeper.alba.mapper.AlbaMapper;
import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.exception.ErrorCode;
import com.jaegokeeper.item.mapper.ItemMapper;
import com.jaegokeeper.common.dto.PageResponse;
import com.jaegokeeper.request.dto.request.RequestCreateBatchRequest;
import com.jaegokeeper.request.dto.request.RequestCreateRequest;
import com.jaegokeeper.request.dto.request.RequestPageRequest;
import com.jaegokeeper.request.dto.request.RequestStatusUpdateRequest;
import com.jaegokeeper.request.dto.request.RequestUpdateRequest;
import com.jaegokeeper.request.dto.response.RequestListResponse;
import com.jaegokeeper.request.enums.RequestType;
import com.jaegokeeper.request.model.Request;
import com.jaegokeeper.request.enums.RequestStatus;
import com.jaegokeeper.request.mapper.RequestMapper;
import com.jaegokeeper.request.service.RequestService;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
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

    private LoginContext login;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        login = new LoginContext(100, 1, "tester", "LOCAL");
    }


    @Test
    public void 요청상태변경_존재하지않는요청_예외() {
        when(requestMapper.findRequestStatus(1, 999)).thenReturn(null);

        RequestStatusUpdateRequest req = new RequestStatusUpdateRequest();
        req.setRequestStatus(RequestStatus.CONFIRM);

        try {
            requestService.updateRequestStatus(login, 999, req);
            fail("BusinessException이 발생해야 합니다");
        } catch (BusinessException e) {
            assertEquals(ErrorCode.REQUEST_NOT_FOUND, e.getErrorCode());
        }
    }

    @Test
    public void 요청상태변경_상태전이불가_예외() {
        when(requestMapper.findRequestStatus(1, 1)).thenReturn(RequestStatus.CONFIRM);
        when(requestMapper.updateRequestStatus(1, 1, RequestStatus.CONFIRM, RequestStatus.CONFIRM)).thenReturn(0);

        RequestStatusUpdateRequest req = new RequestStatusUpdateRequest();
        req.setRequestStatus(RequestStatus.CONFIRM);

        try {
            requestService.updateRequestStatus(login, 1, req);
            fail("BusinessException이 발생해야 합니다");
        } catch (BusinessException e) {
            assertEquals(ErrorCode.STATE_CONFLICT, e.getErrorCode());
        }
    }

    @Test
    public void 요청상태변경_이미완료된요청_예외() {
        when(requestMapper.findRequestStatus(1, 1)).thenReturn(RequestStatus.DONE);

        RequestStatusUpdateRequest req = new RequestStatusUpdateRequest();
        req.setRequestStatus(RequestStatus.CONFIRM);

        try {
            requestService.updateRequestStatus(login, 1, req);
            fail("BusinessException이 발생해야 합니다");
        } catch (BusinessException e) {
            assertEquals(ErrorCode.REQUEST_ALREADY_CLOSED, e.getErrorCode());
        }
    }

    @Test
    public void 요청상태변경_이미취소된요청_예외() {
        when(requestMapper.findRequestStatus(1, 1)).thenReturn(RequestStatus.CANCEL);

        RequestStatusUpdateRequest req = new RequestStatusUpdateRequest();
        req.setRequestStatus(RequestStatus.WAIT);

        try {
            requestService.updateRequestStatus(login, 1, req);
            fail("BusinessException이 발생해야 합니다");
        } catch (BusinessException e) {
            assertEquals(ErrorCode.REQUEST_ALREADY_CLOSED, e.getErrorCode());
        }
    }

    @Test
    public void 요청상태변경_성공() {
        when(requestMapper.findRequestStatus(1, 1)).thenReturn(RequestStatus.WAIT);
        when(requestMapper.updateRequestStatus(1, 1, RequestStatus.WAIT, RequestStatus.CONFIRM)).thenReturn(1);

        RequestStatusUpdateRequest req = new RequestStatusUpdateRequest();
        req.setRequestStatus(RequestStatus.CONFIRM);

        requestService.updateRequestStatus(login, 1, req);

        verify(requestMapper).updateRequestStatus(1, 1, RequestStatus.WAIT, RequestStatus.CONFIRM);
    }


    @Test
    public void 요청상세_존재하지않는요청_예외() {
        when(requestMapper.findRequestDetail(1, 999)).thenReturn(null);

        try {
            requestService.getRequestDetail(login, 999);
            fail("BusinessException이 발생해야 합니다");
        } catch (BusinessException e) {
            assertEquals(ErrorCode.REQUEST_NOT_FOUND, e.getErrorCode());
        }
    }

    private RequestCreateRequest createReq(Integer itemId, Integer albaId, RequestType type, Integer amount) {
        RequestCreateRequest r = new RequestCreateRequest();
        r.setItemId(itemId);
        r.setAlbaId(albaId);
        r.setRequestType(type);
        r.setRequestAmount(amount);
        return r;
    }

    private RequestCreateBatchRequest batch(RequestCreateRequest... requests) {
        RequestCreateBatchRequest batch = new RequestCreateBatchRequest();
        batch.setRequests(List.of(requests));
        return batch;
    }

    @Test
    public void 요청생성_내매장_상품이_아니면_예외() {
        when(itemMapper.countByStoreIdAndItemId(1, 10)).thenReturn(0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> requestService.createRequest(login, batch(createReq(10, 5, RequestType.CHECK, 0))));

        assertEquals(ErrorCode.ITEM_NOT_FOUND, e.getErrorCode());
        verify(requestMapper, never()).insertRequest(any());
    }

    @Test
    public void 요청생성_내매장_알바가_아니면_예외() {
        when(itemMapper.countByStoreIdAndItemId(1, 10)).thenReturn(1);
        when(albaMapper.countByStoreIdAndAlbaId(1, 5)).thenReturn(0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> requestService.createRequest(login, batch(createReq(10, 5, RequestType.CHECK, 0))));

        assertEquals(ErrorCode.ALBA_NOT_IN_STORE, e.getErrorCode());
    }

    @Test
    public void 요청생성_발주는_수량이_1_이상이어야_함() {
        when(itemMapper.countByStoreIdAndItemId(1, 10)).thenReturn(1);
        when(albaMapper.countByStoreIdAndAlbaId(1, 5)).thenReturn(1);

        BusinessException e = assertThrows(BusinessException.class,
                () -> requestService.createRequest(login, batch(createReq(10, 5, RequestType.ORDER, 0))));

        assertEquals(ErrorCode.REQUEST_AMOUNT_INVALID, e.getErrorCode());
    }

    @Test
    public void 요청생성_여러건_성공시_등록_건수_반환() {
        when(itemMapper.countByStoreIdAndItemId(anyInt(), anyInt())).thenReturn(1);
        when(albaMapper.countByStoreIdAndAlbaId(anyInt(), anyInt())).thenReturn(1);
        when(requestMapper.insertRequest(any(Request.class))).thenReturn(1);

        int created = requestService.createRequest(login, batch(
                createReq(10, 5, RequestType.ORDER, 5),
                createReq(11, 5, RequestType.CHECK, 0)));

        assertEquals(2, created);
        verify(requestMapper, times(2)).insertRequest(any(Request.class));
    }

    @Test
    public void 요청생성_저장_실패시_예외() {
        when(itemMapper.countByStoreIdAndItemId(1, 10)).thenReturn(1);
        when(albaMapper.countByStoreIdAndAlbaId(1, 5)).thenReturn(1);
        when(requestMapper.insertRequest(any(Request.class))).thenReturn(0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> requestService.createRequest(login, batch(createReq(10, 5, RequestType.CHECK, 0))));

        assertEquals(ErrorCode.INTERNAL_ERROR, e.getErrorCode());
    }

    @Test
    public void 요청삭제_대상없으면_예외() {
        when(requestMapper.softDeleteRequest(1, 7)).thenReturn(0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> requestService.softDeleteRequest(login, 7));

        assertEquals(ErrorCode.REQUEST_NOT_FOUND, e.getErrorCode());
    }

    @Test
    public void 요청삭제_성공() {
        when(requestMapper.softDeleteRequest(1, 7)).thenReturn(1);

        requestService.softDeleteRequest(login, 7);

        verify(requestMapper).softDeleteRequest(1, 7);
    }

    @Test
    public void 요청수정_대상없으면_예외() {
        when(requestMapper.findRequestStatus(1, 5)).thenReturn(null);

        BusinessException e = assertThrows(BusinessException.class,
                () -> requestService.updateRequest(login, 5, new RequestUpdateRequest()));

        assertEquals(ErrorCode.REQUEST_NOT_FOUND, e.getErrorCode());
    }

    @Test
    public void 요청수정_대기상태가_아니면_예외() {
        when(requestMapper.findRequestStatus(1, 5)).thenReturn(RequestStatus.CONFIRM);

        BusinessException e = assertThrows(BusinessException.class,
                () -> requestService.updateRequest(login, 5, new RequestUpdateRequest()));

        assertEquals(ErrorCode.REQUEST_STATUS_NOT_WAIT, e.getErrorCode());
        verify(requestMapper, never()).updateRequest(anyInt(), anyInt(), any());
    }

    @Test
    public void 요청수정_발주로_바꾸면서_수량이_0이면_예외() {
        when(requestMapper.findRequestStatus(1, 5)).thenReturn(RequestStatus.WAIT);
        RequestUpdateRequest req = new RequestUpdateRequest();
        req.setRequestType(RequestType.ORDER);
        req.setRequestAmount(0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> requestService.updateRequest(login, 5, req));

        assertEquals(ErrorCode.REQUEST_AMOUNT_INVALID, e.getErrorCode());
    }

    @Test
    public void 요청수정_다른_매장_알바로_바꾸면_예외() {
        when(requestMapper.findRequestStatus(1, 5)).thenReturn(RequestStatus.WAIT);
        when(albaMapper.countByStoreIdAndAlbaId(1, 9)).thenReturn(0);
        RequestUpdateRequest req = new RequestUpdateRequest();
        req.setAlbaId(9);

        BusinessException e = assertThrows(BusinessException.class,
                () -> requestService.updateRequest(login, 5, req));

        assertEquals(ErrorCode.ALBA_NOT_IN_STORE, e.getErrorCode());
    }

    @Test
    public void 요청수정_갱신된_행이_없으면_대기상태_아님_예외() {
        when(requestMapper.findRequestStatus(1, 5)).thenReturn(RequestStatus.WAIT);
        RequestUpdateRequest req = new RequestUpdateRequest();
        when(requestMapper.updateRequest(1, 5, req)).thenReturn(0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> requestService.updateRequest(login, 5, req));

        assertEquals(ErrorCode.REQUEST_STATUS_NOT_WAIT, e.getErrorCode());
    }

    @Test
    public void 요청수정_성공() {
        when(requestMapper.findRequestStatus(1, 5)).thenReturn(RequestStatus.WAIT);
        RequestUpdateRequest req = new RequestUpdateRequest();
        req.setRequestAmount(3);
        when(requestMapper.updateRequest(1, 5, req)).thenReturn(1);

        requestService.updateRequest(login, 5, req);

        verify(requestMapper).updateRequest(1, 5, req);
    }

    @Test
    public void 요청목록_페이지와_개수로_조회_위치를_계산() {
        RequestPageRequest page = new RequestPageRequest();
        page.setPage(2);
        page.setSize(10);
        page.setType(RequestType.ORDER);
        List<RequestListResponse> content = List.of(new RequestListResponse());
        when(requestMapper.countRequestList(1, RequestType.ORDER, null)).thenReturn(25);
        when(requestMapper.findRequestList(1, RequestType.ORDER, null, 10, 10)).thenReturn(content);

        PageResponse<RequestListResponse> result = requestService.getRequestList(login, page);

        assertEquals(content, result.getContent());
        assertEquals(Integer.valueOf(25), result.getTotalElements());
        assertEquals(Integer.valueOf(3), result.getTotalPages());
    }
}
