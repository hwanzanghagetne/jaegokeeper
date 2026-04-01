package com.jaegokeeper.request.dto.request;

import com.jaegokeeper.common.dto.PageRequest;
import com.jaegokeeper.request.enums.RequestStatus;
import com.jaegokeeper.request.enums.RequestType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class RequestPageRequest extends PageRequest {

    private RequestType type;
    private RequestStatus requestStatus;
}
