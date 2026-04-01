package com.jaegokeeper.board.dto.request;

import com.jaegokeeper.board.enums.BoardWriterType;
import com.jaegokeeper.image.dto.ImageInfoDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
public class BoardUpdateRequest extends ImageInfoDTO {

    private String title;

    private String content;

    private BoardWriterType writerType;

    private Integer writerId;

    private Boolean removeImage;
}
