package com.jaegokeeper.hwan.request.service;

import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.common.dto.PageResponse;
import com.jaegokeeper.hwan.alba.mapper.AlbaMapper2;
import com.jaegokeeper.hwan.item.mapper.ItemMapper;
import com.jaegokeeper.hwan.request.domain.Request;
import com.jaegokeeper.hwan.request.dto.request.*;
import com.jaegokeeper.hwan.request.dto.response.RequestDetailResponse;
import com.jaegokeeper.hwan.request.dto.response.RequestListResponse;
import com.jaegokeeper.hwan.request.enums.RequestStatus;
import com.jaegokeeper.hwan.request.enums.RequestType;
import com.jaegokeeper.hwan.request.mapper.RequestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.jaegokeeper.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestMapper requestMapper;
    private final ItemMapper itemMapper;
    private final AlbaMapper2 albaMapper2;

    // 생성
    @Transactional
    @Override
    public int createRequest(Integer storeId, RequestCreateBatchRequest dto) {
        int createdCount = 0;

        for (RequestCreateRequest reqDto : dto.getRequests()) {

            Integer itemId = reqDto.getItemId();
            if (itemMapper.countByStoreIdAndItemId(storeId, itemId) != 1) {
                throw new BusinessException(ITEM_NOT_FOUND);
            }

            Integer albaId = reqDto.getAlbaId();
            if (albaMapper2.countByStoreIdAndAlbaId(storeId, albaId) != 1) {
                throw new BusinessException(ALBA_NOT_IN_STORE);
            }

            RequestType requestType = reqDto.getRequestType();
            Integer requestAmount = reqDto.getRequestAmount();
            LocalDateTime requestDate = reqDto.getRequestDate();

            if (requestType == RequestType.ORDER && requestAmount < 1) {
                throw new BusinessException(BAD_REQUEST);
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

    // 상세 조회
    @Override
    public RequestDetailResponse getRequestDetail(Integer storeId, Integer requestId) {
        RequestDetailResponse dto = requestMapper.findRequestDetail(storeId, requestId);
        if (dto == null) {
            throw new BusinessException(REQUEST_NOT_FOUND);
        }
        return dto;
    }

    // 리스트 조회
    @Override
    public PageResponse<RequestListResponse> getRequestList(Integer storeId, RequestPageRequest dto) {

        int pageNum = dto.getPageValue();
        int pageSize = dto.getSizeValue();
        RequestType requestType = dto.getType();
        RequestStatus requestStatus = dto.getRequestStatus();

        int offset = (pageNum - 1) * pageSize;

        int totalElements = requestMapper.countRequestList(storeId, requestType, requestStatus);
        List<RequestListResponse> content = requestMapper.findRequestList(storeId, requestType, requestStatus, offset, pageSize);

        return PageResponse.of(content, pageNum, pageSize, totalElements);
    }

    //삭제
    @Transactional
    @Override
    public void softDeleteRequest(Integer storeId, Integer requestId) {
        int deleted = requestMapper.softDeleteRequest(storeId, requestId);
        if (deleted != 1) {
            throw new BusinessException(REQUEST_NOT_FOUND);
        }
    }

    // 수정
    @Transactional
    @Override
    public void updateRequest(Integer storeId, Integer requestId, RequestUpdateRequest dto) {

        RequestStatus status = requestMapper.findRequestStatus(storeId, requestId);
        if (status == null) {
            throw new BusinessException(REQUEST_NOT_FOUND);
        }
        if (status != RequestStatus.WAIT) {
            throw new BusinessException(REQUEST_STATUS_NOT_WAIT);
        }

        if (dto.getRequestType() == RequestType.ORDER
                && dto.getRequestAmount() != null && dto.getRequestAmount() < 1) {
            throw new BusinessException(BAD_REQUEST);
        }

        int updated = requestMapper.updateRequest(storeId, requestId, dto);
        if (updated != 1) {
            throw new BusinessException(INTERNAL_ERROR);
        }
    }

    // 상태 수정
    @Transactional
    @Override
    public void updateRequestStatus(Integer storeId, Integer requestId, RequestStatusUpdateRequest dto) {
        int updated = requestMapper.updateRequestStatus(storeId, requestId, dto.getRequestStatus());
        if (updated != 1) {
            throw new BusinessException(REQUEST_NOT_FOUND);
        }
    }
}
