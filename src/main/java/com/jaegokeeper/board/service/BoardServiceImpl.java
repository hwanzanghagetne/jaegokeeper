package com.jaegokeeper.board.service;

import com.jaegokeeper.board.dto.request.BoardCreateRequest;
import com.jaegokeeper.board.dto.request.BoardPageRequest;
import com.jaegokeeper.board.dto.request.BoardUpdateRequest;
import com.jaegokeeper.board.dto.response.BoardDetailResponse;
import com.jaegokeeper.board.domain.Board;
import com.jaegokeeper.board.dto.response.BoardListResponse;
import com.jaegokeeper.board.dto.BoardUpdateParam;
import com.jaegokeeper.board.enums.BoardSearchType;
import com.jaegokeeper.board.enums.BoardType;
import com.jaegokeeper.board.enums.BoardWriterType;
import com.jaegokeeper.board.mapper.BoardMapper;
import com.jaegokeeper.ddan.img.service.ImgService;
import com.jaegokeeper.hwan.alba.mapper.AlbaMapper2;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.hwan.item.dto.response.ItemPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

import static com.jaegokeeper.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService{

    private final ImgService imgService;
    private final BoardMapper boardMapper;
    private final AlbaMapper2 albaMapper2;

    // 리스트 조회
    @Override
    public ItemPageResponse<BoardListResponse> getBoardList(Integer storeId, BoardPageRequest dto) {

        int page = dto.getPageValue();
        int size = dto.getSizeValue();
        int offset = (page - 1) * size;

        String keyword = dto.getKeywordValue();
        BoardSearchType searchType = dto.getSearchTypeValue();
        BoardType boardType = dto.getType();

        int totalElements = boardMapper.countBoardList(storeId, boardType,keyword,searchType);
        List<BoardListResponse> content = boardMapper.findBoardList(storeId, boardType, keyword, searchType, size, offset);

        int totalPages = (totalElements + size - 1) / size;

        return new ItemPageResponse<>(content, page, size, totalElements, totalPages);
    }

    // 상세 조회
    @Override
    public BoardDetailResponse getBoardDetail(Integer storeId, Integer boardId) {
        BoardDetailResponse dto = boardMapper.getBoardDetail(storeId, boardId);
        if (dto == null) {
            throw new BusinessException(BOARD_NOT_FOUND);
        }
        return dto;
    }

    // 생성
    @Transactional
    @Override
    public Integer createBoard(Integer storeId, BoardType boardType, BoardCreateRequest dto) {

        BoardWriterType writerType = dto.getWriterType();
        if (writerType == null) {
            throw new BusinessException(BAD_REQUEST);
        }

        String writer;

        if (writerType == BoardWriterType.ANONYMOUS) {
            //익명인데 writerId
            if (dto.getWriterId() != null) {
                throw new BusinessException(BAD_REQUEST);
            }
            writer = "익명";
        } else { // ALBA
            Integer writerId = dto.getWriterId();
            if (writerId == null) {
                throw new BusinessException(BAD_REQUEST);
            }
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

        Board board = Board.create(
                storeId,
                boardType,
                dto.getTitle(),
                dto.getContent(),
                writer,
                imageId);
        int insertedBoard = boardMapper.insertBoard(board);
        if (insertedBoard != 1) {
            throw new BusinessException(INTERNAL_ERROR);
        }
        return board.getBoardId();
    }

    // 수정
    @Override
    @Transactional
    public void updateBoard(Integer storeId, Integer boardId, BoardUpdateRequest dto) {

        int exists = boardMapper.countActiveByStoreIdAndBoardId(storeId, boardId);
        if (exists != 1) {
            throw new BusinessException(BOARD_NOT_FOUND);
        }

        String writer = null;

        BoardWriterType writerType = dto.getWriterType();
        if (writerType != null) {

            if (writerType == BoardWriterType.ANONYMOUS) {
                //익명인데 writerId
                if (dto.getWriterId() != null) {
                    throw new BusinessException(BAD_REQUEST);
                }
                writer = "익명";
            } else { // ALBA
                Integer writerId = dto.getWriterId();
                if (writerId == null) {
                    throw new BusinessException(BAD_REQUEST);
                }

                int count = albaMapper2.countByStoreIdAndAlbaId(storeId, writerId);
                if (count != 1) {
                    throw new BusinessException(ALBA_NOT_IN_STORE);
                }
                writer = albaMapper2.findAlbaNameByAlbaId(writerId);
            }
        }

        BoardUpdateParam updateBoard = new BoardUpdateParam(
                dto.getTitle(),
                dto.getContent(),
                writer,
                dto.getImageId()
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
