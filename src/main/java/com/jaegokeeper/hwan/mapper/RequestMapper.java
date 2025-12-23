package com.jaegokeeper.hwan.mapper;

import com.jaegokeeper.hwan.domain.Request;
import com.jaegokeeper.hwan.domain.enums.RequestStatus;
import com.jaegokeeper.hwan.domain.enums.RequestType;
import com.jaegokeeper.hwan.dto.AlbaOptionDTO;
import com.jaegokeeper.hwan.dto.RequestListDTO;
import com.jaegokeeper.hwan.dto.RequestStatusUpdateRequestDTO;
import com.jaegokeeper.hwan.dto.RequestUpdateRequestDTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RequestMapper {

    int insertRequest(Request request);

    long countRequestList(@Param("storeId") Integer storeId,
                          @Param("requestType") RequestType requestType,
                          @Param("requestStatus") RequestStatus requestStatus);


    List<RequestListDTO> findRequestList(@Param("storeId") Integer storeId,
                                         @Param("requestType") RequestType requestType,
                                         @Param("requestStatus") RequestStatus requestStatus,
                                         @Param("offset") int offset,
                                         @Param("size") int size);

    List<AlbaOptionDTO> findAlbaOptionsForRequest(@Param("storeId") Integer storeId);

    //삭제
    int softDeleteRequest(@Param("storeId") Integer storeId,
                          @Param("requestId") Integer requestId);

    //수정
    int updateRequest(@Param("storeId") Integer storeId,
                      @Param("requestId") Integer requestId,
                      @Param("dto") RequestUpdateRequestDTO dto);

    //상태 수정
    int updateRequestStatus(@Param("storeId") Integer storeId,
                            @Param("requestId") Integer requestId,
                            @Param("dto") RequestStatusUpdateRequestDTO dto);
}
