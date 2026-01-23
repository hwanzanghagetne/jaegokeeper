package com.jaegokeeper.board.service;

import com.jaegokeeper.board.dto.*;
import com.jaegokeeper.board.enums.BoardType;
import com.jaegokeeper.board.mapper.BoardMapper;
import com.jaegokeeper.ddan.img.service.ImgService;
import com.jaegokeeper.hwan.alba.mapper.AlbaMapper2;
import com.jaegokeeper.hwan.exception.NotFoundException;
import com.jaegokeeper.hwan.item.dto.PageResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService{

    private final ImgService imgImplement;
    private final BoardMapper boardMapper;
    private final AlbaMapper2 albaMapper2;

    @Override
    public PageResponseDTO<BoardListDTO> getBoardList(Integer storeId, BoardPageRequestDTO dto) {
        int page = dto.getPageValue();
        int size = dto.getSizeValue();

        int totalElements = boardMapper.countBoardList(storeId, dto.getType());
        int totalPages = (totalElements + size - 1) / size;

        int offset = (page - 1) * size;

        List<BoardListDTO> content = boardMapper.findBoardList(storeId,dto.getType(),size,offset);

        return new PageResponseDTO<>(content, page, size, totalElements, totalPages);
    }

    @Override
    public BoardDetailResponseDTO getBoardDetail(Integer storeId, Integer boardId) {
        BoardDetailResponseDTO dto = boardMapper.getBoardDetail(storeId, boardId);
        if (dto == null) {
            throw new NotFoundException("존재하지 않는 게시글입니다.");
        }
        return dto;
    }

    // 생성
    @Transactional
    @Override
    public void createBoard(Integer storeId, BoardType boardType, BoardCreateRequestDTO dto) {

        String writer;
        Integer writerId = dto.getWriterId();
        if (writerId == null) {
            writer = "익명";
        } else {
            int count = albaMapper2.countByStoreIdAndAlbaId(storeId, writerId);
            if (count != 1) {
                throw new NotFoundException("해당 매장 알바생이 아닙니다.");
            }
            writer = albaMapper2.findAlbaNameByAlbaId(writerId);
        }
        int imageId = 0;
        try {
            imageId = imgImplement.uploadImg(dto);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        BoardInsertDTO board = new BoardInsertDTO(storeId, boardType, dto.getTitle(), dto.getContent(), writer, imageId);
        int insertedBoard = boardMapper.insertBoard(board);
        if (insertedBoard != 1) {
            throw new IllegalStateException("board 생성 실패");
        }
    }

    // 수정
    @Override
    @Transactional
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
            int count = albaMapper2.countByStoreIdAndAlbaId(storeId, writerId);
            if (count != 1) {
                throw new NotFoundException("해당 매장 직원이 아닙니다.");
            }
            writer = albaMapper2.findAlbaNameByAlbaId(writerId);
        }

        BoardUpdateDTO updateBoard = new BoardUpdateDTO(
                dto.getTitle(), dto.getContent(), writer, dto.getImageId()
        );

        int updatedBoard = boardMapper.updateBoard(storeId,boardId,updateBoard);
        if (updatedBoard != 1) {
            throw new IllegalStateException("게시글 수정 실패");
        }
    }

    // 삭제
    @Override
    @Transactional
    public void softDeleteBoard(Integer storeId, Integer boardId) {
        int exists = boardMapper.countActiveByStoreIdAndBoardId(storeId, boardId);
        if (exists != 1) {
            throw new NotFoundException("해당 게시글이 없습니다.");
        }

        int deletedBoard = boardMapper.softDeleteBoard(storeId, boardId);
        if (deletedBoard != 1) {
            throw new IllegalStateException("삭제 실패");
        }
    }


}
