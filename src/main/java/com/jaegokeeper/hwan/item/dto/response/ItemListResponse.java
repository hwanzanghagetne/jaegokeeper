package com.jaegokeeper.hwan.item.dto.response;

import com.jaegokeeper.hwan.request.enums.RequestStatus;
import com.jaegokeeper.hwan.request.enums.RequestType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class ItemListResponse {
    //item
    private Integer itemId;
    private Integer stockId;
    private String itemName;

    //stock
    private Integer stockAmount;
    private Boolean isPinned;

    //이미지 일단 null
    private Integer imageId;

    private Integer latestRequestId;
    private RequestType latestRequestType;
    private Integer latestRequestAmount;
    private RequestStatus latestRequestStatus;
    private LocalDateTime latestRequestCreatedAt;


}
