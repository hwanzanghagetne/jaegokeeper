package com.jaegokeeper.hwan.request.dto;


import com.jaegokeeper.hwan.request.enums.RequestStatus;
import com.jaegokeeper.hwan.request.enums.RequestType;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class RequestListDTO {

    private Integer requestId;
    private RequestType requestType;
    private String itemName;
    private Integer requestAmount;
    private LocalDateTime requestDate;
    private String albaName;
    private RequestStatus requestStatus;
    private LocalDateTime createdAt;
}
