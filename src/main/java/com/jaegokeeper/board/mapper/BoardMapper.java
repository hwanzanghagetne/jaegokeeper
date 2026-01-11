package com.jaegokeeper.board.mapper;

import com.jaegokeeper.board.dto.BoardCreateRequestDTO;
import com.jaegokeeper.board.dto.BoardInsertDTO;
import com.jaegokeeper.board.dto.BoardListDto;
import com.jaegokeeper.board.dto.BoardUpdateDTO;
import com.jaegokeeper.board.enums.BoardType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BoardMapper {

    // 게시판 목록 조회 페이지(타입별)
    List<BoardListDto> getBoardByType(@Param("storeId") Integer storeId,
                                      @Param("boardType") BoardType boardType);


    // 상세 페이지
    BoardListDto detailBoard(@Param("boardId") int boardId);

    // 등록 페이지
    int insertBoard(BoardInsertDTO dto);

    // 수정 페이지
    int updateBoard(BoardUpdateDTO dto);

    // 삭제 페이지

    int deleteBoard(@Param("boardId") int boardId);

    // 게시글이 해당 스토어 소속인지 검증
    int countActiveByStoreIdAndBoardId(@Param("storeId") Integer storeId,
                                       @Param("boardId") Integer boardId);
}
