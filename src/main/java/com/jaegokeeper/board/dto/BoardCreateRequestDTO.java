package com.jaegokeeper.board.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;


@Getter
@NoArgsConstructor
public class BoardCreateRequestDTO {

    @NotBlank
    private String title;

    @NotBlank
    private String content;


    private Integer writerId;


    private Integer imageId;


}
