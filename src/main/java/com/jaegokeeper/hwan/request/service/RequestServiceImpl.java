package com.jaegokeeper.hwan.request.service;

import com.jaegokeeper.hwan.alba.dto.AlbaOptionDTO;
import com.jaegokeeper.hwan.exception.NotFoundException;
import com.jaegokeeper.hwan.item.dto.PageResponseDTO;
import com.jaegokeeper.hwan.request.domain.Request;
import com.jaegokeeper.hwan.request.dto.*;
import com.jaegokeeper.hwan.request.enums.RequestStatus;
import com.jaegokeeper.hwan.request.enums.RequestType;
import com.jaegokeeper.hwan.request.mapper.RequestMapper;
import com.jaegokeeper.hwan.stock.mapper.StockMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RequestServiceImpl implements RequestService {

    private final RequestMapper requestMapper;
    private final StockMapper stockMapper;

    @Transactional
    @Override
    public int createRequest(Integer storeId, RequestCreateBatchRequestDTO dto) {

        int createdCount = 0;
        //시간 분 허용
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);

        for (RequestCreateRequestDTO reqDto : dto.getRequests()) {

            Integer stockId = reqDto.getStockId();
            Integer count = stockMapper.countByStockIdAndStoreId(stockId, storeId);
            if (count != 1) {
                throw new NotFoundException("해당 매장의 재고가 아닙니다. stockId=" + stockId);
            }

            RequestType requestType = reqDto.getRequestType();
            Integer requestAmount = reqDto.getRequestAmount();
            LocalDateTime requestDate = reqDto.getRequestDate();

            if (requestType == RequestType.입고요청 && requestAmount < 1) {
                    throw new IllegalArgumentException("입고요청은 수량이 1 이상 필수입니다.");
            }
            if (requestDate == null) {
                requestDate = now;
            }
            if (requestDate.isBefore(now)) {
                throw new IllegalArgumentException("요청 날짜는 과거로 선택할 수 없습니다.");
            }

            Request request = new Request();
            request.setStockId(stockId);
            request.setAlbaId(reqDto.getAlbaId());
            request.setRequestType(requestType);
            request.setRequestAmount(requestAmount);
            request.setRequestDate(requestDate);
            request.setRequestStatus(RequestStatus.대기);

            int inserted = requestMapper.insertRequest(request);
            if (inserted != 1) {
                throw new IllegalStateException("요청 등록 실패");
            }
            createdCount++;
        }
        return createdCount;
    }

    @Override
    public PageResponseDTO<RequestListDTO> getRequestList(RequestPageRequestDTO dto) {

        Integer storeId = dto.getStoreId();
        int pageNum = dto.getPageValue();
        int pageSize = dto.getSizeValue();
        RequestType requestType = dto.getRequestType();
        RequestStatus requestStatus = dto.getRequestStatus();

        int offset = (pageNum - 1) * pageSize;

        int totalElements = requestMapper.countRequestList(storeId, requestType, requestStatus);
        List<RequestListDTO> content = requestMapper.findRequestList(storeId, requestType, requestStatus, offset, pageSize);
        int totalPages = (int) Math.ceil(((double) totalElements / pageSize));

        return new PageResponseDTO<>(content, pageNum, pageSize, totalElements, totalPages);
    }

    // 임시 요청용 알바 리스트
    @Override
    public List<AlbaOptionDTO> findAlbaOptionsForRequest(Integer storeId) {
        List<AlbaOptionDTO> albaOptionsForRequest = requestMapper.findAlbaOptionsForRequest(storeId);
        return albaOptionsForRequest;
    }


    //삭제
    @Transactional
    @Override
    public void deleteRequest(Integer storeId, Integer requestId) {
        int deleted = requestMapper.softDeleteRequest(storeId, requestId);
        if (deleted != 1) {
            throw new IllegalStateException("삭제 실패");
        }
    }

    // 수정
    @Transactional
    @Override
    public void updateRequest(Integer storeId, Integer requestId, RequestUpdateRequestDTO dto) {
        int updated = requestMapper.updateRequest(storeId, requestId, dto);
        if (updated != 1) {
            throw new IllegalStateException("수정 실패");
        }
    }

    // 상태 수정
    @Transactional
    @Override
    public void updateRequestStatus(Integer storeId, Integer requestId, RequestStatusUpdateRequestDTO dto) {
        int updated = requestMapper.updateRequestStatus(storeId, requestId, dto);
        if (updated != 1) {
            throw new IllegalStateException("상태 수정 실패");
        }
    }
}
