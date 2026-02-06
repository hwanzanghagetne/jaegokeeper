package com.jaegokeeper.board.dto.request;

import com.jaegokeeper.board.enums.BoardWriterType;
import com.jaegokeeper.ddan.img.dto.ImgInfoDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
public class BoardUpdateRequest extends ImgInfoDTO {

    private String title;

    private String content;

    private BoardWriterType writerType;

    private Integer writerId;

    private Boolean removeImage;
}
