package com.jaegokeeper.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class BoardDetailResponseDTO {

    private Integer boardId;
    private String title;
    private String content;
    private String writer;
    private LocalDateTime updatedAt;
    private Integer imageId;


}
