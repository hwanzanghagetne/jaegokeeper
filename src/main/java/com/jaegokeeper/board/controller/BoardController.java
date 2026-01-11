package com.jaegokeeper.board.controller;

import com.jaegokeeper.board.dto.BoardCreateRequestDTO;
import com.jaegokeeper.board.dto.BoardDetailResponseDTO;
import com.jaegokeeper.board.dto.BoardUpdateRequestDTO;
import com.jaegokeeper.board.enums.BoardType;
import com.jaegokeeper.board.service.BoardService;
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
        return ResponseEntity.noContent().build();
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
