package com.jaegokeeper.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

@Getter
@Setter
@NoArgsConstructor
public abstract class PageRequest {

    @Min(value = 1, message = "page는 1 이상입니다.")
    private Integer page;

    @Min(value = 1, message = "size는 1 이상입니다.")
    @Max(value = 50, message = "size는 50 이하입니다.")
    private Integer size;

    @Schema(hidden = true)
    public int getPageValue() {
        return page == null ? 1 : page;
    }

    @Schema(hidden = true)
    public int getSizeValue() {
        return size == null ? 10 : size;
    }
}
