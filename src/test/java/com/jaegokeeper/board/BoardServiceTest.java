package com.jaegokeeper.board;

import com.jaegokeeper.alba.mapper.AlbaMapper;
import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.board.dto.BoardUpdateParamImg;
import com.jaegokeeper.board.dto.request.BoardCreateRequest;
import com.jaegokeeper.board.dto.request.BoardPageRequest;
import com.jaegokeeper.board.dto.request.BoardUpdateRequest;
import com.jaegokeeper.board.dto.response.BoardListResponse;
import com.jaegokeeper.board.enums.BoardSearchType;
import com.jaegokeeper.board.enums.BoardType;
import com.jaegokeeper.board.enums.BoardWriterType;
import com.jaegokeeper.board.mapper.BoardMapper;
import com.jaegokeeper.board.model.Board;
import com.jaegokeeper.board.service.BoardService;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.exception.ErrorCode;
import com.jaegokeeper.common.dto.PageResponse;
import com.jaegokeeper.image.dto.ImageInfoDTO;
import com.jaegokeeper.image.service.ImageService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BoardServiceTest {

    @InjectMocks
    private BoardService boardService;

    @Mock
    private ImageService imgService;

    @Mock
    private BoardMapper boardMapper;

    @Mock
    private AlbaMapper albaMapper;

    private LoginContext login;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        login = new LoginContext(10, 1, "tester", "LOCAL");
    }

    @Test
    public void 게시글작성_작성자유형없음_예외() {
        BoardCreateRequest req = new BoardCreateRequest();
        req.setTitle("공지");
        req.setContent("내용");

        BusinessException e = assertThrows(BusinessException.class,
                () -> boardService.createBoard(login, BoardType.NOTICE, req));

        assertEquals(ErrorCode.INVALID_WRITER_INFO, e.getErrorCode());
    }

    @Test
    public void 게시글작성_익명인데작성자ID있음_예외() {
        BoardCreateRequest req = new BoardCreateRequest();
        req.setTitle("공지");
        req.setContent("내용");
        req.setWriterType(BoardWriterType.ANONYMOUS);
        req.setWriterId(5);

        BusinessException e = assertThrows(BusinessException.class,
                () -> boardService.createBoard(login, BoardType.NOTICE, req));

        assertEquals(ErrorCode.INVALID_WRITER_INFO, e.getErrorCode());
    }

    @Test
    public void 게시글작성_알바인데작성자ID없음_예외() {
        BoardCreateRequest req = new BoardCreateRequest();
        req.setTitle("공지");
        req.setContent("내용");
        req.setWriterType(BoardWriterType.ALBA);

        BusinessException e = assertThrows(BusinessException.class,
                () -> boardService.createBoard(login, BoardType.NOTICE, req));

        assertEquals(ErrorCode.INVALID_WRITER_INFO, e.getErrorCode());
    }

    @Test
    public void 게시글작성_작성자가매장소속아님_예외() {
        BoardCreateRequest req = new BoardCreateRequest();
        req.setTitle("공지");
        req.setContent("내용");
        req.setWriterType(BoardWriterType.ALBA);
        req.setWriterId(5);

        when(albaMapper.countByStoreIdAndAlbaId(1, 5)).thenReturn(0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> boardService.createBoard(login, BoardType.NOTICE, req));

        assertEquals(ErrorCode.ALBA_NOT_IN_STORE, e.getErrorCode());
    }

    @Test
    public void 게시글작성_익명_성공() {
        BoardCreateRequest req = new BoardCreateRequest();
        req.setTitle("공지");
        req.setContent("내용");
        req.setWriterType(BoardWriterType.ANONYMOUS);

        doAnswer(invocation -> {
            Board board = invocation.getArgument(0);
            board.setBoardId(200);
            return 1;
        }).when(boardMapper).insertBoard(any(Board.class));

        Integer boardId = boardService.createBoard(login, BoardType.NOTICE, req);

        assertEquals(Integer.valueOf(200), boardId);
    }

    @Test
    public void 게시글수정_대상없음_예외() {
        BoardUpdateRequest req = new BoardUpdateRequest();
        req.setTitle("수정된 제목");

        when(boardMapper.countActiveByStoreIdAndBoardId(1, 5)).thenReturn(0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> boardService.updateBoard(login, 5, req));

        assertEquals(ErrorCode.BOARD_NOT_FOUND, e.getErrorCode());
    }

    @Test
    public void 게시글삭제_대상없음_예외() {
        when(boardMapper.countActiveByStoreIdAndBoardId(1, 5)).thenReturn(0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> boardService.softDeleteBoard(login, 5));

        assertEquals(ErrorCode.BOARD_NOT_FOUND, e.getErrorCode());
    }

    @Test
    public void 게시글상세조회_없음_예외() {
        when(boardMapper.getBoardDetail(1, 5)).thenReturn(null);

        BusinessException e = assertThrows(BusinessException.class,
                () -> boardService.getBoardDetail(login, 5));

        assertEquals(ErrorCode.BOARD_NOT_FOUND, e.getErrorCode());
    }

    private MockMultipartFile imageFile() {
        return new MockMultipartFile("file", "a.png", "image/png", new byte[]{1, 2, 3});
    }

    private ImageInfoDTO uploaded(int imageId, String imagePath) {
        ImageInfoDTO info = new ImageInfoDTO();
        info.setImageId(imageId);
        info.setImagePath(imagePath);
        return info;
    }

    @Test
    public void 게시글목록_페이지와_개수로_조회_위치를_계산() {
        BoardPageRequest page = new BoardPageRequest();
        page.setPage(3);
        page.setSize(5);
        List<BoardListResponse> content = List.of(new BoardListResponse());
        when(boardMapper.countBoardList(1, null, null, BoardSearchType.ALL)).thenReturn(12);
        when(boardMapper.findBoardList(1, null, null, BoardSearchType.ALL, 5, 10)).thenReturn(content);

        PageResponse<BoardListResponse> result = boardService.getBoardList(login, page);

        assertEquals(content, result.getContent());
        assertEquals(Integer.valueOf(3), result.getTotalPages());
    }

    @Test
    public void 게시글작성_알바_작성자는_알바_이름으로_저장() {
        BoardCreateRequest req = new BoardCreateRequest();
        req.setTitle("공지");
        req.setContent("내용");
        req.setWriterType(BoardWriterType.ALBA);
        req.setWriterId(5);
        when(albaMapper.countByStoreIdAndAlbaId(1, 5)).thenReturn(1);
        when(albaMapper.findAlbaNameByAlbaId(5)).thenReturn("홍길동");
        doAnswer(invocation -> {
            Board board = invocation.getArgument(0);
            board.setBoardId(300);
            return 1;
        }).when(boardMapper).insertBoard(any(Board.class));

        boardService.createBoard(login, BoardType.NOTICE, req);

        ArgumentCaptor<Board> captor = ArgumentCaptor.forClass(Board.class);
        verify(boardMapper).insertBoard(captor.capture());
        assertEquals("홍길동", captor.getValue().getWriter());
        assertEquals(Integer.valueOf(1), captor.getValue().getStoreId());
        assertEquals(BoardType.NOTICE, captor.getValue().getBoardType());
    }

    @Test
    public void 게시글작성_저장_실패시_올린_이미지_삭제() {
        BoardCreateRequest req = new BoardCreateRequest();
        req.setTitle("공지");
        req.setContent("내용");
        req.setWriterType(BoardWriterType.ANONYMOUS);
        req.setFile(imageFile());
        when(imgService.uploadIfPresent(any())).thenReturn(uploaded(9, "2026/11/10/a.png"));
        when(boardMapper.insertBoard(any(Board.class))).thenThrow(new IllegalStateException("db down"));

        assertThrows(IllegalStateException.class,
                () -> boardService.createBoard(login, BoardType.NOTICE, req));

        verify(imgService).deleteImageFile("2026/11/10/a.png");
    }

    @Test
    public void 게시글수정_이미지_삭제_요청은_변경_파라미터에_반영() {
        when(boardMapper.countActiveByStoreIdAndBoardId(1, 5)).thenReturn(1);
        when(boardMapper.updateBoardImg(eq(1), eq(5), any(BoardUpdateParamImg.class))).thenReturn(1);
        BoardUpdateRequest req = new BoardUpdateRequest();
        req.setTitle("새 제목");
        req.setRemoveImage(true);

        boardService.updateBoard(login, 5, req);

        ArgumentCaptor<BoardUpdateParamImg> captor = ArgumentCaptor.forClass(BoardUpdateParamImg.class);
        verify(boardMapper).updateBoardImg(eq(1), eq(5), captor.capture());
        assertEquals("새 제목", captor.getValue().getTitle());
        assertEquals(Boolean.TRUE, captor.getValue().getRemoveImage());
        assertNull(captor.getValue().getImageId());
    }

    @Test
    public void 게시글수정_갱신_실패시_새로_올린_이미지_삭제() {
        when(boardMapper.countActiveByStoreIdAndBoardId(1, 5)).thenReturn(1);
        BoardUpdateRequest req = new BoardUpdateRequest();
        req.setFile(imageFile());
        when(imgService.uploadForUpdate(any(), any())).thenReturn(uploaded(9, "2026/11/10/b.png"));
        when(boardMapper.updateBoardImg(eq(1), eq(5), any(BoardUpdateParamImg.class))).thenReturn(0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> boardService.updateBoard(login, 5, req));

        assertEquals(ErrorCode.BOARD_NOT_FOUND, e.getErrorCode());
        verify(imgService).deleteImageFile("2026/11/10/b.png");
    }

    @Test
    public void 게시글삭제_성공() {
        when(boardMapper.countActiveByStoreIdAndBoardId(1, 5)).thenReturn(1);
        when(boardMapper.softDeleteBoard(1, 5)).thenReturn(1);

        boardService.softDeleteBoard(login, 5);

        verify(boardMapper).softDeleteBoard(1, 5);
    }
}
