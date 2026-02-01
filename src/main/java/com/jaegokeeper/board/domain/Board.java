package com.jaegokeeper.board.domain;

import com.jaegokeeper.board.enums.BoardType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class Board {

    @Setter
    private Integer boardId;

    private Integer storeId;
    private BoardType boardType;
    private String title;
    private String content;
    private String writer;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Boolean isActive;
    private Boolean isPinned;

    private Integer imageId;

    public static Board create(Integer storeId, BoardType boardType, String title, String content, String writer, Integer imageId) {
        Board board = new Board();
        board.storeId = storeId;
        board.boardType = boardType;
        board.title = title;
        board.content = content;
        board.writer = writer;
        board.imageId = imageId;

        return board;
    }
}
