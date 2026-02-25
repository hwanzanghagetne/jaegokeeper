package com.jaegokeeper.hwan.request.service;

import com.jaegokeeper.common.dto.PageResponse;
import com.jaegokeeper.hwan.request.dto.request.RequestCreateBatchRequest;
import com.jaegokeeper.hwan.request.dto.request.RequestPageRequest;
import com.jaegokeeper.hwan.request.dto.request.RequestStatusUpdateRequest;
import com.jaegokeeper.hwan.request.dto.request.RequestUpdateRequest;
import com.jaegokeeper.hwan.request.dto.response.RequestDetailResponse;
import com.jaegokeeper.hwan.request.dto.response.RequestListResponse;

public interface RequestService {


    // 생성
    int createRequest(Integer storeId, RequestCreateBatchRequest dto);

    //리스트
    PageResponse<RequestListResponse> getRequestList(Integer storeId, RequestPageRequest dto);

    //삭제
    void softDeleteRequest(Integer storeId, Integer requestId);

    //수정
    void updateRequest(Integer storeId, Integer requestId, RequestUpdateRequest dto);

    //상태 수정
    void updateRequestStatus(Integer storeId, Integer requestId, RequestStatusUpdateRequest dto);

    RequestDetailResponse getRequestDetail(Integer storeId, Integer requestId);
}
