package com.jaegokeeper.hwan.request.dto.request;

import com.jaegokeeper.hwan.request.enums.RequestType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class RequestUpdateRequest {

    private RequestType requestType;

    @Min(0)
    private Integer requestAmount;

    @Min(1)
    private Integer albaId;

    private LocalDateTime requestDate;
}
