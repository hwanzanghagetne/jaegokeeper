package com.jaegokeeper.board.dto.request;

import com.jaegokeeper.board.enums.BoardWriterType;
import com.jaegokeeper.ddan.img.dto.ImgInfoDTO;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;


@Getter
@Setter
@NoArgsConstructor
public class BoardCreateRequest extends ImgInfoDTO {

    @NotBlank(message = "title 필수입니다.")
    private String title;

    @NotBlank(message = "content 필수입니다.")
    private String content;

    private BoardWriterType writerType;

    @ApiModelProperty(
            value = """
        작성자 ID  
        - writerType = ALBA -> 필수  
        - writerType = ANONYMOUS -> null
        """
    )
    private Integer writerId;




}
