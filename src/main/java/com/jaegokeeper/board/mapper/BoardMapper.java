package com.jaegokeeper.board.mapper;

import com.jaegokeeper.board.dto.BoardDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BoardMapper {
    // 게시판 목록 조회 페이지(타입별)
    List<BoardDto> getBoardByType(@Param("boardType") String boardType);

    // 상세 페이지
    BoardDto detailBoard(@Param("boardId") int boardId);

    // 등록 페이지
    int insertBoard(BoardDto boardDto);

    // 수정 페이지
    int updateBoard(BoardDto boardDto);

    // 삭제 페이지
    int deleteBoard(@Param("boardId") int boardId);
}
