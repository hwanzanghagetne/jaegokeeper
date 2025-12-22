package com.jaegokeeper.hwan.domain;

import com.jaegokeeper.hwan.domain.enums.RequestStatus;
import com.jaegokeeper.hwan.domain.enums.RequestType;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class Request {

    private Integer requestId;
    private Integer stockId;
    private Integer albaId;
    private Integer requestAmount;
    private LocalDateTime requestDate;
    private RequestType requestType;
    private RequestStatus requestStatus;

    private Boolean delYn;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}
