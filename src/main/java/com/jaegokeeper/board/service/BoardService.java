package com.jaegokeeper.board.service;

import com.jaegokeeper.board.dto.BoardUpdateParamImg;
import com.jaegokeeper.board.dto.request.BoardCreateRequest;
import com.jaegokeeper.board.dto.request.BoardPageRequest;
import com.jaegokeeper.board.dto.request.BoardUpdateRequest;
import com.jaegokeeper.board.dto.response.BoardDetailResponse;
import com.jaegokeeper.board.model.Board;
import com.jaegokeeper.board.dto.response.BoardListResponse;
import com.jaegokeeper.board.enums.BoardSearchType;
import com.jaegokeeper.board.enums.BoardType;
import com.jaegokeeper.board.enums.BoardWriterType;
import com.jaegokeeper.board.mapper.BoardMapper;
import com.jaegokeeper.image.dto.ImageInfoDTO;
import com.jaegokeeper.image.service.ImageService;
import com.jaegokeeper.alba.mapper.AlbaMapper;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.common.dto.PageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

import static com.jaegokeeper.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final ImageService imgService;
    private final BoardMapper boardMapper;
    private final AlbaMapper albaMapper2;

    public PageResponse<BoardListResponse> getBoardList(Integer storeId, BoardPageRequest dto) {

        int page = dto.getPageValue();
        int size = dto.getSizeValue();
        int offset = (page - 1) * size;

        String keyword = dto.getKeywordValue();
        BoardSearchType searchType = dto.getSearchTypeValue();
        BoardType boardType = dto.getType();

        int totalElements = boardMapper.countBoardList(storeId, boardType, keyword, searchType);
        List<BoardListResponse> content = boardMapper.findBoardList(storeId, boardType, keyword, searchType, size, offset);

        return PageResponse.of(content, page, size, totalElements);
    }

    public BoardDetailResponse getBoardDetail(Integer storeId, Integer boardId) {
        BoardDetailResponse dto = boardMapper.getBoardDetail(storeId, boardId);
        if (dto == null) {
            throw new BusinessException(BOARD_NOT_FOUND);
        }
        return dto;
    }

    @Transactional
    public Integer createBoard(Integer storeId, BoardType boardType, BoardCreateRequest dto) {

        BoardWriterType writerType = dto.getWriterType();
        if (writerType == null) {
            throw new BusinessException(INVALID_WRITER_INFO);
        }
        String writer = resolveWriter(storeId, writerType, dto.getWriterId());

        Integer imageId = uploadImageIfPresent(dto);

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

    @Transactional
    public void updateBoard(Integer storeId, Integer boardId, BoardUpdateRequest dto) {

        int exists = boardMapper.countActiveByStoreIdAndBoardId(storeId, boardId);
        if (exists != 1) {
            throw new BusinessException(BOARD_NOT_FOUND);
        }

        BoardWriterType writerType = dto.getWriterType();
        String writer = (writerType != null) ? resolveWriter(storeId, writerType, dto.getWriterId()) : null;

        boolean wantsRemove = Boolean.TRUE.equals(dto.getRemoveImage());
        Integer newImageId = resolveImageIdForUpdate(dto.getFile(), dto.getRemoveImage(), dto);

        BoardUpdateParamImg updateBoard = new BoardUpdateParamImg(
                dto.getTitle(),
                dto.getContent(),
                writer,
                newImageId,
                wantsRemove ? true : null
        );

        int updatedBoard = boardMapper.updateBoardImg(storeId, boardId, updateBoard);
        if (updatedBoard != 1) {
            throw new BusinessException(BOARD_NOT_FOUND);
        }
    }

    @Transactional
    public void softDeleteBoard(Integer storeId, Integer boardId) {
        int exists = boardMapper.countActiveByStoreIdAndBoardId(storeId, boardId);
        if (exists != 1) {
            throw new BusinessException(BOARD_NOT_FOUND);
        }

        int deletedBoard = boardMapper.softDeleteBoard(storeId, boardId);
        if (deletedBoard != 1) {
            throw new BusinessException(BOARD_NOT_FOUND);
        }
    }

    private String resolveWriter(Integer storeId, BoardWriterType writerType, Integer writerId) {
        if (writerType == BoardWriterType.ANONYMOUS) {
            if (writerId != null) {
                throw new BusinessException(INVALID_WRITER_INFO);
            }
            return "익명";
        }
        if (writerId == null) {
            throw new BusinessException(INVALID_WRITER_INFO);
        }
        if (albaMapper2.countByStoreIdAndAlbaId(storeId, writerId) != 1) {
            throw new BusinessException(ALBA_NOT_IN_STORE);
        }
        return albaMapper2.findAlbaNameByAlbaId(writerId);
    }

    private Integer uploadImageIfPresent(ImageInfoDTO dto) {
        if (dto.getFile() == null || dto.getFile().isEmpty()) return null;
        return imgService.uploadImg(dto);
    }

    private Integer resolveImageIdForUpdate(MultipartFile file, Boolean removeImage, ImageInfoDTO dto) {
        boolean hasFile = (file != null && !file.isEmpty());
        boolean wantsRemove = Boolean.TRUE.equals(removeImage);
        if (wantsRemove && hasFile) {
            throw new BusinessException(IMAGE_UPDATE_CONFLICT);
        }
        if (!hasFile) return null;
        return imgService.uploadImg(dto);
    }

}
