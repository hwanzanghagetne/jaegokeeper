package com.jaegokeeper.board.dto;

import com.jaegokeeper.board.enums.BoardType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BoardInsertDTO {

    private final Integer storeId;
    private final BoardType boardType;
    private final String title;
    private final String content;
    private final String writer;
    private final Integer imageId;
}
