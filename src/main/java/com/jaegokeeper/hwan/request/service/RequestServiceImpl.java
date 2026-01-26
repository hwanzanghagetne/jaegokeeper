package com.jaegokeeper.hwan.request.service;

import com.jaegokeeper.hwan.alba.dto.AlbaOptionDTO;
import com.jaegokeeper.hwan.alba.mapper.AlbaMapper2;
import com.jaegokeeper.hwan.exception.BusinessException;
import com.jaegokeeper.hwan.item.dto.PageResponseDTO;
import com.jaegokeeper.hwan.item.mapper.ItemMapper;
import com.jaegokeeper.hwan.request.domain.Request;
import com.jaegokeeper.hwan.request.dto.*;
import com.jaegokeeper.hwan.request.enums.RequestStatus;
import com.jaegokeeper.hwan.request.enums.RequestType;
import com.jaegokeeper.hwan.request.mapper.RequestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.jaegokeeper.hwan.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestMapper requestMapper;
    private final ItemMapper itemMapper;
    private final AlbaMapper2 albaMapper2;
    @Transactional
    @Override
    public int createRequest(Integer storeId, RequestCreateBatchRequestDTO dto) {
        int createdCount = 0;

        for (RequestCreateRequestDTO reqDto : dto.getRequests()) {

            Integer itemId = reqDto.getItemId();
            int count = itemMapper.countByStoreIdAndItemId(storeId, itemId);
            if (count != 1) {
                throw new BusinessException(ITEM_NOT_FOUND);
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

    @Override
    public PageResponseDTO<RequestListDTO> getRequestList(Integer storeId, RequestPageRequestDTO dto) {

        int pageNum = dto.getPageValue();
        int pageSize = dto.getSizeValue();
        RequestType requestType = dto.getType();
        RequestStatus requestStatus = dto.getRequestStatus();

        int offset = (pageNum - 1) * pageSize;

        int totalElements = requestMapper.countRequestList(storeId, requestType, requestStatus);
        List<RequestListDTO> content = requestMapper.findRequestList(storeId, requestType, requestStatus, offset, pageSize);
        int totalPages = (totalElements + pageSize - 1) / pageSize;

        return new PageResponseDTO<>(content, pageNum, pageSize, totalElements, totalPages);
    }

    // 임시 요청용 알바 리스트
    @Override
    public List<AlbaOptionDTO> findAlbaOptionsForRequest(Integer storeId) {
        return albaMapper2.findAlbaOptionsForRequest(storeId);
    }


    //삭제
    @Transactional
    @Override
    public void softDeleteRequest(Integer storeId, Integer requestId) {
        int deleted = requestMapper.softDeleteRequest(storeId, requestId);
        if (deleted != 1) {
            throw new BusinessException(INTERNAL_ERROR);
        }
    }

    // 수정
    @Transactional
    @Override
    public void updateRequest(Integer storeId, Integer requestId, RequestUpdateRequestDTO dto) {
        int updated = requestMapper.updateRequest(storeId, requestId, dto);
        if (updated != 1) {
            throw new BusinessException(INTERNAL_ERROR);
        }
    }

    // 상태 수정
    @Transactional
    @Override
    public void updateRequestStatus(Integer storeId, Integer requestId, RequestStatusUpdateRequestDTO dto) {
        int updated = requestMapper.updateRequestStatus(storeId, requestId, dto);
        if (updated != 1) {
            throw new BusinessException(INTERNAL_ERROR);
        }
    }
}
