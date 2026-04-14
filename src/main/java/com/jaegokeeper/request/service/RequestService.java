package com.jaegokeeper.request.service;

import com.jaegokeeper.alba.mapper.AlbaMapper;
import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.common.dto.PageResponse;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.item.mapper.ItemMapper;
import com.jaegokeeper.request.model.Request;
import com.jaegokeeper.request.dto.request.RequestCreateBatchRequest;
import com.jaegokeeper.request.dto.request.RequestCreateRequest;
import com.jaegokeeper.request.dto.request.RequestPageRequest;
import com.jaegokeeper.request.dto.request.RequestStatusUpdateRequest;
import com.jaegokeeper.request.dto.request.RequestUpdateRequest;
import com.jaegokeeper.request.dto.response.RequestDetailResponse;
import com.jaegokeeper.request.dto.response.RequestListResponse;
import com.jaegokeeper.request.enums.RequestStatus;
import com.jaegokeeper.request.enums.RequestType;
import com.jaegokeeper.request.mapper.RequestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.jaegokeeper.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class RequestService {

    private final RequestMapper requestMapper;
    private final ItemMapper itemMapper;
    private final AlbaMapper albaMapper;

    @Transactional
    public int createRequest(LoginContext login, Integer storeId, RequestCreateBatchRequest dto) {
        validateStoreAccess(login, storeId);
        int createdCount = 0;

        for (RequestCreateRequest reqDto : dto.getRequests()) {
            Integer itemId = reqDto.getItemId();
            if (itemMapper.countByStoreIdAndItemId(storeId, itemId) != 1) {
                throw new BusinessException(ITEM_NOT_FOUND);
            }

            Integer albaId = reqDto.getAlbaId();
            if (albaMapper.countByStoreIdAndAlbaId(storeId, albaId) != 1) {
                throw new BusinessException(ALBA_NOT_IN_STORE);
            }

            RequestType requestType = reqDto.getRequestType();
            Integer requestAmount = reqDto.getRequestAmount();
            LocalDateTime requestDate = reqDto.getRequestDate();

            if (requestType == RequestType.ORDER && requestAmount < 1) {
                throw new BusinessException(REQUEST_AMOUNT_INVALID);
            }

            Request request = Request.create(itemId, reqDto.getAlbaId(), requestType, requestAmount, requestDate);
            int inserted = requestMapper.insertRequest(request);
            if (inserted != 1) {
                throw new BusinessException(INTERNAL_ERROR);
            }
            createdCount++;
        }
        return createdCount;
    }

    public RequestDetailResponse getRequestDetail(LoginContext login, Integer storeId, Integer requestId) {
        validateStoreAccess(login, storeId);
        RequestDetailResponse dto = requestMapper.findRequestDetail(storeId, requestId);
        if (dto == null) {
            throw new BusinessException(REQUEST_NOT_FOUND);
        }
        return dto;
    }

    public PageResponse<RequestListResponse> getRequestList(LoginContext login, Integer storeId, RequestPageRequest dto) {
        validateStoreAccess(login, storeId);
        int pageNum = dto.getPageValue();
        int pageSize = dto.getSizeValue();
        RequestType requestType = dto.getType();
        RequestStatus requestStatus = dto.getRequestStatus();

        int offset = (pageNum - 1) * pageSize;
        int totalElements = requestMapper.countRequestList(storeId, requestType, requestStatus);
        List<RequestListResponse> content = requestMapper.findRequestList(storeId, requestType, requestStatus, offset, pageSize);

        return PageResponse.of(content, pageNum, pageSize, totalElements);
    }

    @Transactional
    public void softDeleteRequest(LoginContext login, Integer storeId, Integer requestId) {
        validateStoreAccess(login, storeId);
        int deleted = requestMapper.softDeleteRequest(storeId, requestId);
        if (deleted != 1) {
            throw new BusinessException(REQUEST_NOT_FOUND);
        }
    }

    @Transactional
    public void updateRequest(LoginContext login, Integer storeId, Integer requestId, RequestUpdateRequest dto) {
        validateStoreAccess(login, storeId);
        RequestStatus status = requestMapper.findRequestStatus(storeId, requestId);
        if (status == null) {
            throw new BusinessException(REQUEST_NOT_FOUND);
        }
        if (status != RequestStatus.WAIT) {
            throw new BusinessException(REQUEST_STATUS_NOT_WAIT);
        }
        if (dto.getRequestType() == RequestType.ORDER
                && dto.getRequestAmount() != null && dto.getRequestAmount() < 1) {
            throw new BusinessException(REQUEST_AMOUNT_INVALID);
        }
        if (dto.getAlbaId() != null
                && albaMapper.countByStoreIdAndAlbaId(storeId, dto.getAlbaId()) != 1) {
            throw new BusinessException(ALBA_NOT_IN_STORE);
        }
        int updated = requestMapper.updateRequest(storeId, requestId, dto);
        if (updated != 1) {
            throw new BusinessException(REQUEST_STATUS_NOT_WAIT);
        }
    }

    @Transactional
    public void updateRequestStatus(LoginContext login, Integer storeId, Integer requestId, RequestStatusUpdateRequest dto) {
        validateStoreAccess(login, storeId);
        RequestStatus status = requestMapper.findRequestStatus(storeId, requestId);
        if (status == null) {
            throw new BusinessException(REQUEST_NOT_FOUND);
        }
        int updated = requestMapper.updateRequestStatus(storeId, requestId, status, dto.getRequestStatus());
        if (updated != 1) {
            throw new BusinessException(STATE_CONFLICT);
        }
    }

    private void validateStoreAccess(LoginContext login, Integer storeId) {
        if (storeId == null) {
            throw new BusinessException(BAD_REQUEST);
        }
        if (login.getStoreId() != storeId.intValue()) {
            throw new BusinessException(FORBIDDEN);
        }
    }
}
