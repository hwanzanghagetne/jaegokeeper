package com.jaegokeeper.board.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BoardUpdateResponse {

    private final String title;
    private final String content;
    private final String writer;
    private final Integer imageId;

}
