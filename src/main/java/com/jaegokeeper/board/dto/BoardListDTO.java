package com.jaegokeeper.board.dto;

import com.jaegokeeper.board.enums.BoardType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class BoardListDTO {

    private Integer boardId;
    private String title;
    private BoardType boardType;
    private Boolean isPinned;
    private String writer;
    private LocalDateTime updatedAt;

}
