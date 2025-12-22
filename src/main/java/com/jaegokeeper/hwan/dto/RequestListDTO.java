package com.jaegokeeper.hwan.dto;


import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class RequestListDTO {

    private Integer requestId;
    private String requestType;
    private String itemName;
    private Integer requestAmount;
    private LocalDateTime requestDate;
    private String albaName;
    private String requestStatus;
    private LocalDateTime createdAt;
}
