package com.jaegokeeper.board.dto.request;

import com.jaegokeeper.board.enums.BoardWriterType;
import com.jaegokeeper.image.dto.ImageInfoDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotBlank;


@Getter
@Setter
@NoArgsConstructor
public class BoardCreateRequest extends ImageInfoDTO {

    @NotBlank(message = "title 필수입니다.")
    private String title;

    @NotBlank(message = "content 필수입니다.")
    private String content;

    private BoardWriterType writerType;

    @Schema(description = "작성자 ID - writerType = ALBA -> 필수, writerType = ANONYMOUS -> null")
    private Integer writerId;
}
