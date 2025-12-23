package com.jaegokeeper.hwan.service;

import com.jaegokeeper.hwan.domain.enums.RequestStatus;
import com.jaegokeeper.hwan.domain.enums.RequestType;
import com.jaegokeeper.hwan.dto.*;

import java.util.List;

public interface RequestService {


    // 생성
    int createRequest(Integer storeId, RequestCreateBatchRequestDTO dto);

    //리스트
    PageResponseDTO<RequestListDTO> getRequestList(Integer storeId, int page, int size,
                                                   RequestType requestType, RequestStatus requestStatus);

    //삭제
    void deleteRequest(Integer storeId, Integer requestId);

    // 임시 요청용 알바 리스트
    List<AlbaOptionDTO> findAlbaOptionsForRequest(Integer storeId);

    //수정
    void updateRequest(Integer storeId, Integer requestId, RequestUpdateRequestDTO dto);

    //상태 수정
    void updateRequestStatus(Integer storeId, Integer requestId, RequestStatusUpdateRequestDTO dto);
}
