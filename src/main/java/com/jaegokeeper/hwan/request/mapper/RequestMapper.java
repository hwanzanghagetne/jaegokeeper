package com.jaegokeeper.hwan.request.mapper;

import com.jaegokeeper.hwan.request.domain.Request;
import com.jaegokeeper.hwan.request.enums.RequestStatus;
import com.jaegokeeper.hwan.request.enums.RequestType;
import com.jaegokeeper.hwan.alba.dto.AlbaOptionDTO;
import com.jaegokeeper.hwan.request.dto.RequestListDTO;
import com.jaegokeeper.hwan.request.dto.RequestStatusUpdateRequestDTO;
import com.jaegokeeper.hwan.request.dto.RequestUpdateRequestDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
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
