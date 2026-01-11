package com.jaegokeeper.hwan.request.domain;

import com.jaegokeeper.hwan.request.enums.RequestStatus;
import com.jaegokeeper.hwan.request.enums.RequestType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class Request {

    private Integer requestId;
    private Integer itemId;
    private Integer albaId;
    private Integer requestAmount;
    private LocalDateTime requestDate;
    private RequestType requestType;
    private RequestStatus requestStatus;


    public static Request create(Integer itemId, Integer albaId, RequestType requestType, Integer requestAmount, LocalDateTime requestDate) {

        Request request = new Request();
        request.setItemId(itemId);
        request.setAlbaId(albaId);
        request.setRequestType(requestType);
        request.setRequestAmount(requestAmount);
        request.setRequestDate(requestDate);
        request.setRequestStatus(RequestStatus.WAIT);
        return request;
    }

}
