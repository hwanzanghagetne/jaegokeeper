package com.jaegokeeper.hwan.dto;

import com.jaegokeeper.hwan.domain.enums.RequestStatus;
import lombok.AllArgsConstructor;
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
