package com.jaegokeeper.request.dto.response;

import com.jaegokeeper.request.enums.RequestStatus;
import com.jaegokeeper.request.enums.RequestType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class RequestDetailResponse {

    private RequestType requestType;
    private RequestStatus requestStatus;
    private String itemName;
    private Integer requestAmount;
    private LocalDateTime requestDate;
    private Integer albaId;
    private String albaName;
}
