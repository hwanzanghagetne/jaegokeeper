package com.jaegokeeper.board.controller;

import com.jaegokeeper.board.dto.request.BoardCreateRequest;
import com.jaegokeeper.board.dto.request.BoardPageRequest;
import com.jaegokeeper.board.dto.request.BoardUpdateRequest;
import com.jaegokeeper.board.dto.response.BoardCreateResponse;
import com.jaegokeeper.board.dto.response.BoardDetailResponse;
import com.jaegokeeper.board.dto.response.BoardListResponse;
import com.jaegokeeper.board.enums.BoardType;
import com.jaegokeeper.board.service.BoardService;
import com.jaegokeeper.hwan.item.dto.response.ItemPageResponse;
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

    // 생성
    @ApiOperation(value = "게시글 등록",  notes = """
    type(게시판 타입)을 받아 게시글을 등록합니다.
    multipart/form-data 요청입니다.
    """)
    @PostMapping
    public ResponseEntity<BoardCreateResponse> createBoard(
            @PathVariable Integer storeId,
            @RequestParam BoardType type,
            @Valid @ModelAttribute BoardCreateRequest dto
    ) {
        Integer boardId = boardService.createBoard(storeId, type, dto);
        return ResponseEntity.ok(new BoardCreateResponse(boardId));
    }

    // 게시판 목록 조회(타입별)
    @ApiOperation(value = "게시글 목록 조회", notes = "매장(storeId)의 게시글 목록을 타입/페이지 조건으로 조회합니다.")
    @GetMapping
    public ResponseEntity<ItemPageResponse<BoardListResponse>> getBoards(
            @PathVariable Integer storeId,
            @Valid @ModelAttribute BoardPageRequest dto
    ) {
        return ResponseEntity.ok(boardService.getBoardList(storeId, dto));
    }

    // 상세
    @ApiOperation(value = "게시글 상세 조회", notes = "boardId로 게시글을 상세 조회합니다.")
    @GetMapping("/{boardId}")
    public ResponseEntity<BoardDetailResponse> getBoardDetail(
            @PathVariable Integer storeId,
            @PathVariable Integer boardId
    ) {
        return ResponseEntity.ok(boardService.getBoardDetail(storeId, boardId));
    }

    // 수정
    @ApiOperation(value = "게시글 수정", notes = "boardId의 게시글을 수정합니다.")
    @PutMapping("/{boardId}")
    public ResponseEntity<Void> updateBoard(
            @PathVariable Integer storeId,
            @PathVariable Integer boardId,
            @Valid @RequestBody BoardUpdateRequest dto
    ) {
        boardService.updateBoard(storeId, boardId, dto);
        return ResponseEntity.noContent().build();
    }

    // 삭제
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
