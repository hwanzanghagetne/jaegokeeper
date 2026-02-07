package com.jaegokeeper.hwan.request.mapper;

import com.jaegokeeper.hwan.request.domain.Request;
import com.jaegokeeper.hwan.request.dto.response.RequestDetailResponse;
import com.jaegokeeper.hwan.request.enums.RequestStatus;
import com.jaegokeeper.hwan.request.enums.RequestType;
import com.jaegokeeper.hwan.request.dto.response.RequestListResponse;
import com.jaegokeeper.hwan.request.dto.request.RequestStatusUpdateRequest;
import com.jaegokeeper.hwan.request.dto.request.RequestUpdateRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface RequestMapper {

    int insertRequest(Request request);


    RequestDetailResponse findRequestDetail(@Param("storeId") Integer storeId,
                                            @Param("requestId") Integer requestId);

    int countRequestList(@Param("storeId") Integer storeId,
                          @Param("requestType") RequestType requestType,
                          @Param("requestStatus") RequestStatus requestStatus);


    List<RequestListResponse> findRequestList(@Param("storeId") Integer storeId,
                                              @Param("requestType") RequestType requestType,
                                              @Param("requestStatus") RequestStatus requestStatus,
                                              @Param("offset") int offset,
                                              @Param("size") int size);

    //삭제
    int softDeleteRequest(@Param("storeId") Integer storeId,
                          @Param("requestId") Integer requestId);

    //수정
    int updateRequest(@Param("storeId") Integer storeId,
                      @Param("requestId") Integer requestId,
                      @Param("dto") RequestUpdateRequest dto);

    //상태 수정
    int updateRequestStatus(@Param("storeId") Integer storeId,
                            @Param("requestId") Integer requestId,
                            @Param("requestStatus") RequestStatus requestStatus);

    RequestStatus findRequestStatus(
            @Param("storeId") Integer storeId,
            @Param("requestId") Integer requestId
    );
}
