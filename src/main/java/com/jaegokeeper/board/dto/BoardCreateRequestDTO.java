package com.jaegokeeper.board.dto;

import com.jaegokeeper.ddan.img.dto.ImgInfoDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;


@Getter
@Setter
@NoArgsConstructor
public class BoardCreateRequestDTO extends ImgInfoDTO {

    @NotBlank(message = "title 필수입니다.")
    private String title;

    @NotBlank(message = "content 필수입니다.")
    private String content;

    @Min(value = 1,message = "writerId는 1이상이어야합니다.")
    private Integer writerId;




}
