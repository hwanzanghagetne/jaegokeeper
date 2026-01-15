package com.jaegokeeper.board.dto;

import com.jaegokeeper.board.enums.BoardType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@Getter
@Setter
@NoArgsConstructor
public class BoardPageRequestDTO {

    @Min(value = 1,message = "page는 1 이상입니다.")
    private Integer page;

    @Min(value = 1,message = "size 1 이상입니다.")
    @Max(value = 50,message = "size 50 이하입니다.")
    private Integer size;

    private BoardType type;

    //기본값 1
    public int getPageValue() {
        return page == null ? 1 : page;
    }
    //기본값 10
    public int getSizeValue() {
        return size == null ? 10 : size;
    }

    }
