package com.jaegokeeper.hwan.request.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Getter
@Setter
public class RequestCreateBatchRequestDTO {

    @NotEmpty(message = "requests는 비어있을 수 없습니다.")
    List<@Valid RequestCreateRequestDTO> requests;
}
