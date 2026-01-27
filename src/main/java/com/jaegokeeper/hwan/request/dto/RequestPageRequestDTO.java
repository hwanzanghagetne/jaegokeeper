package com.jaegokeeper.hwan.request.dto;

import com.jaegokeeper.hwan.request.enums.RequestStatus;
import com.jaegokeeper.hwan.request.enums.RequestType;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Getter
@Setter
@NoArgsConstructor
public class RequestPageRequestDTO {
    @Min(value = 1,message = "page 1 이상입니다.")
    private Integer page;

    @Min(value = 1,message = "size 1 이상입니다.")
    @Max(value = 50,message = "size 50 이하입니다.")
    private Integer size;

    private RequestType type;
    private RequestStatus requestStatus;

    //기본값 1
    @ApiModelProperty(hidden = true)
    public int getPageValue() {
        return page == null ? 1 : page;
    }
    //기본값 10
    @ApiModelProperty(hidden = true)
    public int getSizeValue() {
        return size == null ? 10 : size;
    }
}
