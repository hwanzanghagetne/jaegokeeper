package com.jaegokeeper.board.mapper;

import com.jaegokeeper.board.dto.response.BoardDetailResponse;
import com.jaegokeeper.board.domain.Board;
import com.jaegokeeper.board.dto.response.BoardListResponse;
import com.jaegokeeper.board.dto.response.BoardUpdateResponse;
import com.jaegokeeper.board.enums.BoardType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BoardMapper {

    // 게시판 목록 조회 페이지(타입별)
    int countBoardList(@Param("storeId") Integer storeId,
                       @Param("boardType") BoardType boardType);

    List<BoardListResponse> findBoardList(@Param("storeId") Integer storeId,
                                          @Param("boardType") BoardType boardType,
                                          @Param("size") Integer size,
                                          @Param("offset") Integer offset);


    // 상세 페이지
    BoardDetailResponse getBoardDetail(@Param("storeId") Integer storeId,
                                       @Param("boardId") Integer boardId);

    // 등록 페이지
    int insertBoard(Board dto);

    // 수정 페이지
    int updateBoard(@Param("storeId") Integer storeId,
                    @Param("boardId") Integer boardId,
                    @Param("dto") BoardUpdateResponse dto);

    // 삭제 페이지

    int softDeleteBoard(@Param("storeId") Integer storeId,
                        @Param("boardId") Integer boardId);

    // 게시글이 해당 스토어 소속인지 검증
    int countActiveByStoreIdAndBoardId(@Param("storeId") Integer storeId,
                                       @Param("boardId") Integer boardId);
}
