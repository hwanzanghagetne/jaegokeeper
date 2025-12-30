package com.jaegokeeper.hwan.request.dto;

import com.jaegokeeper.hwan.request.enums.RequestStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class RequestStatusUpdateRequestDTO {

    @NotNull(message = "requestStatus는 필수입니다.")
    private RequestStatus requestStatus;
}
