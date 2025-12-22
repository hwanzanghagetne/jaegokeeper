package com.jaegokeeper.hwan.mapper;

import com.jaegokeeper.hwan.domain.Request;
import com.jaegokeeper.hwan.domain.enums.RequestStatus;
import com.jaegokeeper.hwan.domain.enums.RequestType;
import com.jaegokeeper.hwan.dto.ItemListDTO;
import com.jaegokeeper.hwan.dto.RequestListDTO;
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
}
