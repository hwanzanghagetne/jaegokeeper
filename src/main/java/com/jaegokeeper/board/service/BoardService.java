package com.jaegokeeper.board.service;

import com.jaegokeeper.board.dto.request.BoardCreateRequest;
import com.jaegokeeper.board.dto.request.BoardPageRequest;
import com.jaegokeeper.board.dto.request.BoardUpdateRequest;
import com.jaegokeeper.board.dto.response.BoardDetailResponse;
import com.jaegokeeper.board.dto.response.BoardListResponse;
import com.jaegokeeper.board.enums.BoardType;
import com.jaegokeeper.hwan.item.dto.response.ItemPageResponse;

public interface BoardService {

    // 게시판 목록 조회 페이지(타입별)
    ItemPageResponse<BoardListResponse> getBoardList(Integer storeId, BoardPageRequest dto);

    // 상세 페이지
    BoardDetailResponse getBoardDetail(Integer storeId, Integer boardId);
    // 등록 페이지
    Integer createBoard(Integer storeId, BoardType boardType, BoardCreateRequest dto);


    // 수정 페이지
    void updateBoard(Integer storeId, Integer boardId, BoardUpdateRequest dto);

    // 삭제 페이지
    void softDeleteBoard(Integer storeId, Integer boardId);
}
