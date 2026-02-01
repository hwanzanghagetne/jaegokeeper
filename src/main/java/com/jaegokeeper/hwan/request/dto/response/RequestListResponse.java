package com.jaegokeeper.hwan.request.dto.response;


import com.jaegokeeper.hwan.request.enums.RequestStatus;
import com.jaegokeeper.hwan.request.enums.RequestType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class RequestListResponse {

    private Integer requestId;
    private RequestType requestType;
    private String itemName;
    private Integer requestAmount;
    private LocalDateTime requestDate;
    private String albaName;
    private RequestStatus requestStatus;
    private LocalDateTime createdAt;
}
