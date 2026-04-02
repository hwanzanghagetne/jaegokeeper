package com.jaegokeeper.request.model;

import com.jaegokeeper.request.enums.RequestStatus;
import com.jaegokeeper.request.enums.RequestType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class Request {

    @Setter
    private Integer requestId;

    private Integer itemId;
    private Integer albaId;
    private Integer requestAmount;
    private LocalDateTime requestDate;
    private RequestType requestType;
    private RequestStatus requestStatus;

    public static Request create(Integer itemId, Integer albaId, RequestType requestType, Integer requestAmount, LocalDateTime requestDate) {
        Request request = new Request();
        request.itemId = itemId;
        request.albaId = albaId;
        request.requestType = requestType;
        request.requestAmount = requestAmount;
        request.requestDate = requestDate;
        request.requestStatus = RequestStatus.WAIT;
        return request;
    }
}
