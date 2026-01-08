package com.jaegokeeper.board.service;

import com.jaegokeeper.board.mapper.BoardMapper;
import org.springframework.stereotype.Service;

@Service
public class BoardService {

    private final BoardMapper boardMapper;
    public BoardService(BoardMapper boardMapper) {
        this.boardMapper = boardMapper;
    }

    // 게시판 목록 조회 페이지(타입별)

    // 상세 페이지

    // 등록 페이지

    // 수정 페이지

    // 삭제 페이지
}
