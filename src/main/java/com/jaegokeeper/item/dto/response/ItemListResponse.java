package com.jaegokeeper.item.dto.response;

import com.jaegokeeper.request.enums.RequestStatus;
import com.jaegokeeper.request.enums.RequestType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ItemListResponse {

    private Integer itemId;
    private Integer stockId;
    private String itemName;
    private Integer stockAmount;
    private Boolean isPinned;
    private Integer imageId;

    private Integer latestRequestId;
    private RequestType latestRequestType;
    private Integer latestRequestAmount;
    private RequestStatus latestRequestStatus;
    private LocalDateTime latestRequestCreatedAt;
}
