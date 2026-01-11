package com.jaegokeeper.board.service;

import com.jaegokeeper.board.dto.BoardCreateRequestDTO;
import com.jaegokeeper.board.dto.BoardUpdateRequestDTO;
import com.jaegokeeper.board.enums.BoardType;

public interface BoardService {

    // 게시판 목록 조회 페이지(타입별)


    // 상세 페이지

    // 등록 페이지
    int createBoard(Integer storeId, BoardType boardType,BoardCreateRequestDTO dto);


    // 수정 페이지
    void updateBoard(Integer storeId, Integer boardId, BoardUpdateRequestDTO dto);

    // 삭제 페이지
    void softDeleteItem(Integer storeId, Integer boardId);
}
