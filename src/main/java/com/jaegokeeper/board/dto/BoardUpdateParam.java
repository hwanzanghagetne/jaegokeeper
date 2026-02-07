package com.jaegokeeper.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BoardUpdateParam {

    private final String title;
    private final String content;
    private final String writer;
    private final Integer imageId;

}
