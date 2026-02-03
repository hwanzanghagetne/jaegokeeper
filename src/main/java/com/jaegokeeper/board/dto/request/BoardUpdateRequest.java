package com.jaegokeeper.board.dto.request;

import com.jaegokeeper.board.enums.BoardWriterType;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

@Getter
@NoArgsConstructor
public class BoardUpdateRequest {

    private String title;

    private String content;

    private BoardWriterType writerType;


    private Integer writerId;

    private Integer imageId;

}
