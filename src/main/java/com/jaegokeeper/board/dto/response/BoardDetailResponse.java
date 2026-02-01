package com.jaegokeeper.board.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class BoardDetailResponse {

    private Integer boardId;
    private String title;
    private String content;
    private String writer;
    private LocalDateTime updatedAt;
    private Integer imageId;


}
