package com.jaegokeeper.board.controller;

import com.jaegokeeper.board.dto.*;
import com.jaegokeeper.board.enums.BoardType;
import com.jaegokeeper.board.service.BoardService;
import com.jaegokeeper.hwan.item.dto.PageResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/stores/{storeId}/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    // 게시판 목록 조회 페이지(타입별)
    // GET /boards?type=NOTICE (또는 FREE, ANONYMOUS)
    @GetMapping
    public ResponseEntity<PageResponseDTO<BoardListDTO>> getBoards(
            @PathVariable Integer storeId,
            @Valid @ModelAttribute BoardPageRequestDTO dto
    ) {
        return ResponseEntity.ok(boardService.getBoardList(storeId, dto));
    }

    // 상세 페이지
    // GET /boards/{boardId}
    @GetMapping("/{boardId}")
    public ResponseEntity<BoardDetailResponseDTO> getBoardDetail(
            @PathVariable Integer storeId,
            @PathVariable Integer boardId
    ) {
        return ResponseEntity.ok(boardService.getBoardDetail(storeId, boardId));
    }
    // 등록 페이지
    // POST /boards?type=NOTICE (또는 FREE, ANONYMOUS)
    @PostMapping
    public ResponseEntity<Void> createBoard(
            @PathVariable Integer storeId,
            @RequestParam BoardType type,
            @Valid @RequestBody BoardCreateRequestDTO dto
    ) {
        boardService.createBoard(storeId, type, dto);
        return ResponseEntity.ok().build();
    }


    // 수정 페이지
    // PUT /boards/{boardId}
    @PutMapping("/{boardId}")
    public ResponseEntity<Void> updateBoard(
            @PathVariable Integer storeId,
            @PathVariable Integer boardId,
            @Valid @RequestBody BoardUpdateRequestDTO dto
    ) {
        boardService.updateBoard(storeId, boardId, dto);
        return ResponseEntity.noContent().build();
    }

    // 삭제 페이지
    // DELETE /boards/{boardId}
    @DeleteMapping("/{boardId}")
    public ResponseEntity<Void> deleteBoard(
            @PathVariable Integer storeId,
            @PathVariable Integer boardId
    ) {
        boardService.softDeleteItem(storeId, boardId);
        return ResponseEntity.noContent().build();
    }
}
