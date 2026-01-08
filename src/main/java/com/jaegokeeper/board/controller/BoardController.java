package com.jaegokeeper.board.controller;

import com.jaegokeeper.board.service.BoardService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/board")
public class BoardController {

    private final BoardService boardService;
    public BoardController(BoardService boardService) {
        this.boardService = boardService;
    }

    // 게시판 목록 조회 페이지(타입별)
    // GET /boards?type=NOTICE (또는 FREE, ANONYMOUS)

    // 상세 페이지
    // GET /board/{boardId}

    // 등록 페이지
    // POST /board/write

    // 수정 페이지
    // PUT /board/{boardId}

    // 삭제 페이지
    // DELETE /board/{boardId}


}
