package com.jaegokeeper.board.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class BoardDetailResponse {

    private Integer boardId;
    private String title;
    private String content;
    private String writer;
    private LocalDateTime updatedAt;
    private Integer imageId;


}
