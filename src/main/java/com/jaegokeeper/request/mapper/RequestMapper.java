package com.jaegokeeper.request.mapper;

import com.jaegokeeper.request.model.Request;
import com.jaegokeeper.request.dto.request.RequestUpdateRequest;
import com.jaegokeeper.request.dto.response.RequestDetailResponse;
import com.jaegokeeper.request.dto.response.RequestListResponse;
import com.jaegokeeper.request.enums.RequestStatus;
import com.jaegokeeper.request.enums.RequestType;
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

    int softDeleteRequest(@Param("storeId") Integer storeId,
                          @Param("requestId") Integer requestId);

    int updateRequest(@Param("storeId") Integer storeId,
                      @Param("requestId") Integer requestId,
                      @Param("dto") RequestUpdateRequest dto);

    int updateRequestStatus(@Param("storeId") Integer storeId,
                            @Param("requestId") Integer requestId,
                            @Param("currentStatus") RequestStatus currentStatus,
                            @Param("requestStatus") RequestStatus requestStatus);

    RequestStatus findRequestStatus(@Param("storeId") Integer storeId,
                                    @Param("requestId") Integer requestId);
}
