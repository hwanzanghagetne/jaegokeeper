package com.jaegokeeper.board.mapper;

import com.jaegokeeper.board.dto.BoardUpdateParamImg;
import com.jaegokeeper.board.dto.response.BoardDetailResponse;
import com.jaegokeeper.board.model.Board;
import com.jaegokeeper.board.dto.response.BoardListResponse;
import com.jaegokeeper.board.enums.BoardSearchType;
import com.jaegokeeper.board.enums.BoardType;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BoardMapper {

    int countBoardList(@Param("storeId") Integer storeId,
                       @Param("boardType") BoardType boardType,
                       @Param("keyword") String keyword,
                       @Param("searchType") BoardSearchType searchType);

    List<BoardListResponse> findBoardList(@Param("storeId") Integer storeId,
                                          @Param("boardType") BoardType boardType,
                                          @Param("keyword") String keyword,
                                          @Param("searchType") BoardSearchType searchType,
                                          @Param("size") Integer size,
                                          @Param("offset") Integer offset);

    BoardDetailResponse getBoardDetail(@Param("storeId") Integer storeId,
                                       @Param("boardId") Integer boardId);

    int insertBoard(Board dto);

    int updateBoardImg(@Param("storeId") Integer storeId,
                       @Param("boardId") Integer boardId,
                       @Param("dto") BoardUpdateParamImg dto);

    int softDeleteBoard(@Param("storeId") Integer storeId,
                        @Param("boardId") Integer boardId);

    int countActiveByStoreIdAndBoardId(@Param("storeId") Integer storeId,
                                       @Param("boardId") Integer boardId);
}
