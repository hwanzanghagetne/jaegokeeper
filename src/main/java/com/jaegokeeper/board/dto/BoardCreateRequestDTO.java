package com.jaegokeeper.board.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;


@Getter
@NoArgsConstructor
public class BoardCreateRequestDTO {

    @NotBlank(message = "title 필수입니다.")
    private String title;

    @NotBlank(message = "content 필수입니다.")
    private String content;

    @Min(value = 1,message = "writerId는 1이상이어야합니다.")
    private Integer writerId;

    @Min(value = 1, message = "imageId는 1 이상이어야 합니다.")
    private Integer imageId;


}
