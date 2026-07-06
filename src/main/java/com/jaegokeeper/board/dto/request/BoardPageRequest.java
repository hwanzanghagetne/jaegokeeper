package com.jaegokeeper.board.dto.request;

import com.jaegokeeper.board.enums.BoardSearchType;
import com.jaegokeeper.board.enums.BoardType;
import com.jaegokeeper.common.dto.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BoardPageRequest extends PageRequest {

    private BoardType type;
    private String keyword;
    private BoardSearchType searchType;

    @Schema(hidden = true)
    public BoardSearchType getSearchTypeValue() {
        return searchType == null ? BoardSearchType.ALL : searchType;
    }

    @Schema(hidden = true)
    public String getKeywordValue() {
        if (keyword == null) {
            return null;
        }
        String trimmed = keyword.trim();
        return trimmed.isBlank() ? null : trimmed;
    }
}
