package com.jaegokeeper.request.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RequestCreateBatchRequest {

    @NotEmpty(message = "requests는 비어있을 수 없습니다.")
    List<@Valid RequestCreateRequest> requests;
}
