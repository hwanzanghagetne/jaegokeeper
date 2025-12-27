package com.jaegokeeper.hwan.item.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ItemPageRequestDTO {
    @NotNull
    @Min(1)
    private Integer storeId;

    @Min(1)
    private Integer page;

    @Min(1)
    @Max(50)
    private Integer size;

    private List<String> filters;
    private String keyword;

    //기본값 1
    public int getPageValue() {
        return page == null ? 1 : page;
    }
    //기본값 10
    public int getSizeValue() {
        return size == null ? 10 : size;
    }

    public String getKeywordValue() {
        if (keyword == null) {
            return null;
        }
            String trimmed = keyword.trim();
            return trimmed.isBlank() ? null : trimmed;
        }
    }
