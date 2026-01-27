package com.jaegokeeper.board.dto;

import com.jaegokeeper.board.enums.BoardType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class BoardInsertDTO {

    private Integer boardId;
    private Integer storeId;
    private BoardType boardType;
    private String title;
    private String content;
    private String writer;
    private Integer imageId;

    public BoardInsertDTO(Integer storeId, BoardType boardType, String title, String content, String writer, Integer imageId) {
        this.storeId = storeId;
        this.boardType = boardType;
        this.title = title;
        this.content = content;
        this.writer = writer;
        this.imageId = imageId;
    }
}
