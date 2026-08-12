package com.jaegokeeper.board.controller;

import com.jaegokeeper.auth.annotation.LoginUser;
import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.board.dto.request.BoardCreateRequest;
import com.jaegokeeper.board.dto.request.BoardPageRequest;
import com.jaegokeeper.board.dto.request.BoardUpdateRequest;
import com.jaegokeeper.board.dto.response.BoardCreateResponse;
import com.jaegokeeper.board.dto.response.BoardDetailResponse;
import com.jaegokeeper.board.dto.response.BoardListResponse;
import com.jaegokeeper.board.enums.BoardType;
import com.jaegokeeper.board.service.BoardService;
import com.jaegokeeper.common.dto.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Tag(name = "Board")
@RestController
@RequestMapping("/stores/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @Operation(summary = "게시글 등록", description = "type(게시판 타입)을 받아 게시글을 등록합니다.")
    @PostMapping
    public ResponseEntity<BoardCreateResponse> createBoard(
            @RequestParam BoardType type,
            @Valid @ModelAttribute BoardCreateRequest dto,
            @LoginUser LoginContext login
    ) {
        Integer boardId = boardService.createBoard(login, type, dto);
        return ResponseEntity.status(201).body(new BoardCreateResponse(boardId));
    }

    @Operation(summary = "게시글 목록 조회", description = "매장의 게시글 목록을 타입/페이지 조건으로 조회합니다.")
    @GetMapping
    public ResponseEntity<PageResponse<BoardListResponse>> getBoards(
            @Valid @ModelAttribute BoardPageRequest dto,
            @LoginUser LoginContext login
    ) {
        return ResponseEntity.ok(boardService.getBoardList(login, dto));
    }

    @Operation(summary = "게시글 상세 조회", description = "boardId로 게시글을 상세 조회합니다.")
    @GetMapping("/{boardId}")
    public ResponseEntity<BoardDetailResponse> getBoardDetail(
            @PathVariable Integer boardId,
            @LoginUser LoginContext login
    ) {
        return ResponseEntity.ok(boardService.getBoardDetail(login, boardId));
    }

    @Operation(summary = "게시글 수정", description = "boardId의 게시글을 수정합니다.")
    @PutMapping(value = "/{boardId}")
    public ResponseEntity<Void> updateBoard(
            @PathVariable Integer boardId,
            @Valid @ModelAttribute BoardUpdateRequest dto,
            @LoginUser LoginContext login
    ) {
        boardService.updateBoard(login, boardId, dto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "게시글 삭제", description = "boardId의 게시글을 삭제합니다.")
    @DeleteMapping("/{boardId}")
    public ResponseEntity<Void> deleteBoard(
            @PathVariable Integer boardId,
            @LoginUser LoginContext login
    ) {
        boardService.softDeleteBoard(login, boardId);
        return ResponseEntity.noContent().build();
    }

}
