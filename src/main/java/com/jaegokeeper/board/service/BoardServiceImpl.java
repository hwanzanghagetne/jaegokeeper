package com.jaegokeeper.board.service;

import com.jaegokeeper.board.dto.*;
import com.jaegokeeper.board.enums.BoardType;
import com.jaegokeeper.board.mapper.BoardMapper;
import com.jaegokeeper.ddan.img.service.ImgService;
import com.jaegokeeper.hwan.alba.mapper.AlbaMapper2;
import com.jaegokeeper.hwan.exception.BusinessException;
import com.jaegokeeper.hwan.item.dto.PageResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

import static com.jaegokeeper.hwan.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService{

    private final ImgService imgService;
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
            throw new BusinessException(BOARD_NOT_FOUND);
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
                throw new BusinessException(ALBA_NOT_IN_STORE);
            }
            writer = albaMapper2.findAlbaNameByAlbaId(writerId);
        }

        Integer imageId = null;
        try {
            if (dto.getFile() != null && !dto.getFile().isEmpty()) {
                imageId = imgService.uploadImg(dto);
            }
        } catch (IOException e) {
            throw new BusinessException(IMAGE_UPLOAD_FAILED, e);
        }

        BoardInsertDTO board = new BoardInsertDTO(storeId, boardType, dto.getTitle(), dto.getContent(), writer, imageId);
        int insertedBoard = boardMapper.insertBoard(board);
        if (insertedBoard != 1) {
            throw new BusinessException(INTERNAL_ERROR);
        }
    }

    // 수정
    @Override
    @Transactional
    public void updateBoard(Integer storeId, Integer boardId, BoardUpdateRequestDTO dto) {
        int exists = boardMapper.countActiveByStoreIdAndBoardId(storeId, boardId);
        if (exists != 1) {
            throw new BusinessException(BOARD_NOT_FOUND);
        }

        String writer;
        Integer writerId = dto.getWriterId();
        if (writerId == null) {
            writer = "익명";
        } else {
            int count = albaMapper2.countByStoreIdAndAlbaId(storeId, writerId);
            if (count != 1) {
                throw new BusinessException(ALBA_NOT_IN_STORE);
            }
            writer = albaMapper2.findAlbaNameByAlbaId(writerId);
        }

        BoardUpdateDTO updateBoard = new BoardUpdateDTO(
                dto.getTitle(), dto.getContent(), writer, dto.getImageId()
        );

        int updatedBoard = boardMapper.updateBoard(storeId,boardId,updateBoard);
        if (updatedBoard != 1) {
            throw new BusinessException(INTERNAL_ERROR);
        }
    }

    // 삭제
    @Override
    @Transactional
    public void softDeleteBoard(Integer storeId, Integer boardId) {
        int exists = boardMapper.countActiveByStoreIdAndBoardId(storeId, boardId);
        if (exists != 1) {
            throw new BusinessException(BOARD_NOT_FOUND);
        }

        int deletedBoard = boardMapper.softDeleteBoard(storeId, boardId);
        if (deletedBoard != 1) {
            throw new BusinessException(INTERNAL_ERROR);
        }
    }


}
