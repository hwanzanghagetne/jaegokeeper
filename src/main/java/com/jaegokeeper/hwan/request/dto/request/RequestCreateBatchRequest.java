package com.jaegokeeper.hwan.request.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RequestCreateBatchRequest {

    @NotEmpty(message = "requests는 비어있을 수 없습니다.")
    List<@Valid RequestCreateRequest> requests;
}
