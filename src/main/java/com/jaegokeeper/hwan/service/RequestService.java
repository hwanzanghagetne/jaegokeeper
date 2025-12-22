package com.jaegokeeper.hwan.service;

import com.jaegokeeper.hwan.domain.enums.RequestStatus;
import com.jaegokeeper.hwan.domain.enums.RequestType;
import com.jaegokeeper.hwan.dto.PageResponseDTO;
import com.jaegokeeper.hwan.dto.RequestCreateBatchRequestDTO;
import com.jaegokeeper.hwan.dto.RequestListDTO;

public interface RequestService {


    int createRequest(Integer storeId, RequestCreateBatchRequestDTO dto);

    PageResponseDTO<RequestListDTO> getRequestList(Integer storeId, int page, int size,
                                                   RequestType requestType, RequestStatus requestStatus);
}
