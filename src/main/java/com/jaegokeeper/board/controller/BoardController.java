package com.jaegokeeper.board.controller;

import com.jaegokeeper.board.dto.*;
import com.jaegokeeper.board.enums.BoardType;
import com.jaegokeeper.board.service.BoardService;
import com.jaegokeeper.hwan.item.dto.PageResponseDTO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(tags = "Board")
@RestController
@RequestMapping("/stores/{storeId}/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    // 게시판 목록 조회 페이지(타입별)
    // GET /boards?type=NOTICE (또는 FREE, ANONYMOUS)
    @ApiOperation(value = "게시글 목록 조회", notes = "매장(storeId)의 게시글 목록을 타입/페이지 조건으로 조회합니다.")
    @GetMapping
    public ResponseEntity<PageResponseDTO<BoardListDTO>> getBoards(
            @PathVariable Integer storeId,
            @Valid @ModelAttribute BoardPageRequestDTO dto
    ) {
        return ResponseEntity.ok(boardService.getBoardList(storeId, dto));
    }

    // 상세 페이지
    // GET /boards/{boardId}
    @ApiOperation(value = "게시글 상세 조회", notes = "boardId로 게시글을 상세 조회합니다.")
    @GetMapping("/{boardId}")
    public ResponseEntity<BoardDetailResponseDTO> getBoardDetail(
            @PathVariable Integer storeId,
            @PathVariable Integer boardId
    ) {
        return ResponseEntity.ok(boardService.getBoardDetail(storeId, boardId));
    }
    // 등록 페이지
    // POST /boards?type=NOTICE (또는 FREE, ANONYMOUS)
    @ApiOperation(value = "게시글 등록",  notes = "type(게시판 타입)을 받아 게시글을 등록합니다.")
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
    @ApiOperation(value = "게시글 수정", notes = "boardId의 게시글을 수정합니다.")
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
    @ApiOperation(value = "게시글 삭제",notes = "boardId의 게시글을 삭제합니다.")
    @DeleteMapping("/{boardId}")
    public ResponseEntity<Void> deleteBoard(
            @PathVariable Integer storeId,
            @PathVariable Integer boardId
    ) {
        boardService.softDeleteBoard(storeId, boardId);
        return ResponseEntity.noContent().build();
    }
}
