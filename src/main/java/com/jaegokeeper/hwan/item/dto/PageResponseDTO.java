package com.jaegokeeper.hwan.item.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PageResponseDTO<T>{

    private List<T> content;
    private Integer page;
    private Integer size;
    private Integer totalElements;
    private Integer totalPages;
}
