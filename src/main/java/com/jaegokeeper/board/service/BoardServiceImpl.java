package com.jaegokeeper.board.service;

import com.jaegokeeper.board.dto.BoardCreateRequestDTO;
import com.jaegokeeper.board.dto.BoardInsertDTO;
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
}
