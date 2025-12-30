package com.jaegokeeper.hwan.request.dto;

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
public class RequestCreateRequestDTO {

    @NotNull(message = "stockId 필수입니다.")
    @Min(1)
    private Integer stockId;

    //이거 맞나? storeId를 기준으로 근무자 리스트를 보내줘야한다.
    @NotNull(message = "albaId 필수입니다.")
    @Min(1)
    private Integer albaId;

    @NotNull(message = "requestType 필수입니다.")
    private RequestType requestType;

    @NotNull(message = "requestAmount 필수입니다.")
    @Min(value = 0,message = "requestAmount는 0 이상입니다.")
    private Integer requestAmount;

    @NotNull(message = "requestDate 필수입니다.")
    private LocalDateTime requestDate;
}
