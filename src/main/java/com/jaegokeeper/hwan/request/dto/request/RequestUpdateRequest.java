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

    @NotNull(message = "requestType은 필수입니다.")
    private RequestType requestType;

    @NotNull(message = "requestAmount 필수입니다.")
    @Min(0)
    private Integer requestAmount;

    @NotNull(message = "albaId는 필수입니다.")
    @Min(1)
    private Integer albaId;

    @NotNull(message = "requestDate는 필수입니다.")
    private LocalDateTime requestDate;
}
