package com.jaegokeeper.board.dto;

import com.jaegokeeper.board.enums.BoardType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoardListDto {
    private Integer boardId;

    private BoardType boardType;

    private String title;

    private String content;

    private String writer;

    private Boolean isPinned;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
