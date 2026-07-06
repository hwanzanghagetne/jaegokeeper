package com.jaegokeeper.item.dto.request;

import com.jaegokeeper.common.dto.PageRequest;
import com.jaegokeeper.item.enums.ItemFilter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ItemPageRequest extends PageRequest {

    private ItemFilter filter;
    private String keyword;
    private Boolean excludeZero;

    @Schema(hidden = true)
    public boolean getExcludeZeroValue() {
        return Boolean.TRUE.equals(excludeZero);
    }

    @Schema(hidden = true)
    public String getKeywordValue() {
        if (keyword == null) return null;
        String trimmed = keyword.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
