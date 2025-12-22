package com.jaegokeeper.hwan.service;

import com.jaegokeeper.hwan.domain.Request;
import com.jaegokeeper.hwan.domain.enums.RequestStatus;
import com.jaegokeeper.hwan.domain.enums.RequestType;
import com.jaegokeeper.hwan.dto.PageResponseDTO;
import com.jaegokeeper.hwan.dto.RequestCreateBatchRequestDTO;
import com.jaegokeeper.hwan.dto.RequestCreateRequestDTO;
import com.jaegokeeper.hwan.dto.RequestListDTO;
import com.jaegokeeper.hwan.mapper.RequestMapper;
import com.jaegokeeper.hwan.mapper.StockMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.validation.Valid;
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
                throw new IllegalArgumentException("해당 매장의 재고가 아닙니다. stockId=" + stockId);
            }

            RequestType requestType = reqDto.getRequestType();
            Integer requestAmount = reqDto.getRequestAmount();
            LocalDateTime requestDate = reqDto.getRequestDate();

            if (requestType == RequestType.입고요청) {
                if (requestAmount == null || requestAmount < 1) {
                    throw new IllegalArgumentException("입고요청은 수량이 1 이상 필수입니다.");
                }
            } else {
                if (requestAmount != null && requestAmount < 0) {
                    throw new IllegalArgumentException("요청 수량은 음수가 될 수 없습니다.");
                }
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

    @Transactional
    @Override
    public PageResponseDTO<RequestListDTO> getRequestList(Integer storeId, int page, int size, RequestType requestType, RequestStatus requestStatus) {

        if (storeId == null || storeId <= 0) {
            throw new IllegalArgumentException("storeId는 필수이며 1 이상이어야 합니다.");
        }
        if (page <= 0) {
            throw new IllegalArgumentException("page는 1 이상이어야 합니다.");
        }
        if (size <= 0 || size > 50) {
            throw new IllegalArgumentException("size는 1 이상 50 이하만 허용됩니다.");
        }


        int offset = (page - 1) * size;

        long totalElements = requestMapper.countRequestList(storeId, requestType, requestStatus);
        List<RequestListDTO> content = requestMapper.findRequestList(storeId, requestType, requestStatus, offset, size);
        int totalPages = (int) Math.ceil(((double) totalElements / size));

        return new PageResponseDTO<>(content, page, size, totalElements, totalPages);
    }
}
