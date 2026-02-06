package com.jaegokeeper.board.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class BoardUpdateParamImg {

    private String title;
    private String content;
    private String writer;

    private Integer imageId;
    private Boolean removeImage;

}
