package com.jaegokeeper.hwan.dto;

import com.jaegokeeper.hwan.domain.enums.RequestType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
public class RequestCreateRequestDTO {

    @NotNull(message = "stockId 필수입니다.")
    private Integer stockId;

    @NotNull(message = "albaId 필수입니다.")
    private Integer albaId;

    @NotNull(message = "requestType 필수입니다.")
    private RequestType requestType;

    private Integer requestAmount;
    private LocalDateTime requestDate;
}
