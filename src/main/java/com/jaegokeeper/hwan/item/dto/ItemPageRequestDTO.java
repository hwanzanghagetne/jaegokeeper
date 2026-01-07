package com.jaegokeeper.hwan.item.dto;

import com.jaegokeeper.hwan.item.enums.ItemFilter;
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

    @Min(value = 1,message = "page는 1 이상입니다.")
    private Integer page;

    @Min(value = 1,message = "size 1 이상입니다.")
    @Max(value = 50,message = "size 50 이하입니다.")
    private Integer size;

    private ItemFilter filter;
    private String keyword;
    private Boolean excludeZero;
    //기본값 1
    public int getPageValue() {
        return page == null ? 1 : page;
    }
    //기본값 10
    public int getSizeValue() {
        return size == null ? 10 : size;
    }

    public boolean isExcludeZero() {
        return Boolean.TRUE.equals(excludeZero);
    }
    public String getKeywordValue() {
        if (keyword == null) {
            return null;
        }
            String trimmed = keyword.trim();
            return trimmed.isBlank() ? null : trimmed;
        }
    }
