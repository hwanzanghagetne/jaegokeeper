package com.jaegokeeper.board.service;

import com.jaegokeeper.board.dto.*;
import com.jaegokeeper.board.enums.BoardType;
import com.jaegokeeper.board.mapper.BoardMapper;
import com.jaegokeeper.hwan.alba.mapper.AlbaMapper;
import com.jaegokeeper.hwan.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService{

    private final BoardMapper boardMapper;
    private final AlbaMapper albaMapper;

    @Override
    public BoardDetailResponseDTO getBoardDetail(Integer storeId, Integer boardId) {
        BoardDetailResponseDTO dto = boardMapper.getBoardDetail(storeId, boardId);
        if (dto == null) {
            throw new NotFoundException("존재하지 않는 게시글입니다.");
        }
        return dto;
    }

    // 생성
    @Override
    public int createBoard(Integer storeId, BoardType boardType, BoardCreateRequestDTO dto) {

        String writer;
        Integer writerId = dto.getWriterId();
        if (writerId == null) {
            writer = "익명";
        } else {
            int count = albaMapper.countByStoreIdAndAlbaId(storeId, writerId);
            if (count != 1) {
                throw new NotFoundException("해당 매장 알바생이 아닙니다.");
            }
            writer = albaMapper.findAlbaNameByAlbaId(writerId);
        }


        BoardInsertDTO board = new BoardInsertDTO(storeId, boardType, dto.getTitle(), dto.getContent(), writer, dto.getImageId());
        int insertedBoard = boardMapper.insertBoard(board);
        if (insertedBoard != 1) {
            throw new IllegalStateException("board 생성 실패");
        }
        return insertedBoard;

    }

    // 수정
    @Override
    public void updateBoard(Integer storeId, Integer boardId, BoardUpdateRequestDTO dto) {
        int exists = boardMapper.countActiveByStoreIdAndBoardId(storeId, boardId);
        if (exists != 1) {
            throw new NotFoundException("해당 게시글이 없습니다.");
        }

        String writer;
        Integer writerId = dto.getWriterId();
        if (writerId == null) {
            writer = "익명";
        } else {
            int count = albaMapper.countByStoreIdAndAlbaId(storeId, writerId);
            if (count != 1) {
                throw new NotFoundException("해당 매장 직원이 아닙니다.");
            }
            writer = albaMapper.findAlbaNameByAlbaId(writerId);
        }

        BoardUpdateDTO updateBoard = new BoardUpdateDTO(
                storeId, dto.getTitle(), dto.getContent(), writer, dto.getImageId()
        );

        int updatedBoard = boardMapper.updateBoard(updateBoard);
        if (updatedBoard != 1) {
            throw new IllegalStateException("게시글 수정 실패");
        }
    }

    // 삭제
    @Override
    public void softDeleteItem(Integer storeId, Integer boardId) {
        int exists = boardMapper.countActiveByStoreIdAndBoardId(storeId, boardId);
        if (exists != 1) {
            throw new NotFoundException("해당 게시글이 없습니다.");
        }

        int deletedBoard = boardMapper.deleteBoard(storeId, boardId);
        if (deletedBoard != 1) {
            throw new IllegalStateException("삭제 실패");
        }
    }


}
