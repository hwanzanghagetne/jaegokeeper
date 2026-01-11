package com.jaegokeeper.hwan.request.service;

import com.jaegokeeper.hwan.alba.dto.AlbaOptionDTO;
import com.jaegokeeper.hwan.item.dto.PageResponseDTO;
import com.jaegokeeper.hwan.request.dto.*;

import java.util.List;

public interface RequestService {


    // 생성
    int createRequest(Integer storeId, RequestCreateBatchRequestDTO dto);

    //리스트
    PageResponseDTO<RequestListDTO> getRequestList(Integer storeId, RequestPageRequestDTO dto);

    //삭제
    void softDeleteRequest(Integer storeId, Integer requestId);

    // 임시 요청용 알바 리스트
    List<AlbaOptionDTO> findAlbaOptionsForRequest(Integer storeId);

    //수정
    void updateRequest(Integer storeId, Integer requestId, RequestUpdateRequestDTO dto);

    //상태 수정
    void updateRequestStatus(Integer storeId, Integer requestId, RequestStatusUpdateRequestDTO dto);
}
