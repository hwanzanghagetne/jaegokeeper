package com.jaegokeeper.board.service;

import com.jaegokeeper.board.dto.*;
import com.jaegokeeper.board.enums.BoardType;
import com.jaegokeeper.hwan.item.dto.PageResponseDTO;

public interface BoardService {

    // 게시판 목록 조회 페이지(타입별)
    PageResponseDTO<BoardListDTO> getBoardList(Integer storeId, BoardPageRequestDTO dto);

    // 상세 페이지
    BoardDetailResponseDTO getBoardDetail(Integer storeId, Integer boardId);
    // 등록 페이지
    void createBoard(Integer storeId, BoardType boardType,BoardCreateRequestDTO dto);


    // 수정 페이지
    void updateBoard(Integer storeId, Integer boardId, BoardUpdateRequestDTO dto);

    // 삭제 페이지
    void softDeleteBoard(Integer storeId, Integer boardId);
}
