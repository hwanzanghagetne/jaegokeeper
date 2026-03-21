package com.jaegokeeper.common.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class PageResponse<T> {

    private List<T> content;
    private Integer page;
    private Integer size;
    private Integer totalElements;
    private Integer totalPages;

    public static <T> PageResponse<T> of(List<T> content, int page, int size, int totalElements) {
        int totalPages = (totalElements + size - 1) / size;
        return new PageResponse<>(content, page, size, totalElements, totalPages);
    }
}