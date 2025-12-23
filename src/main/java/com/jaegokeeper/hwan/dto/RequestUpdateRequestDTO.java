package com.jaegokeeper.hwan.dto;

import com.jaegokeeper.hwan.domain.enums.RequestType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class RequestUpdateRequestDTO {

    @NotNull(message = "requestType은 필수입니다.")
    private RequestType requestType;

    @NotNull(message = "requestAmount 필수입니다.")
    private Integer requestAmount;

    @NotNull(message = "albaId는 필수입니다.")
    private Integer albaId;

    @NotNull(message = "requestDate는 필수입니다.")
    private LocalDateTime requestDate;
}
