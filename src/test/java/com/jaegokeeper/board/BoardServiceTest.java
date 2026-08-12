package com.jaegokeeper.board;

import com.jaegokeeper.alba.mapper.AlbaMapper;
import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.board.dto.request.BoardCreateRequest;
import com.jaegokeeper.board.dto.request.BoardUpdateRequest;
import com.jaegokeeper.board.enums.BoardType;
import com.jaegokeeper.board.enums.BoardWriterType;
import com.jaegokeeper.board.mapper.BoardMapper;
import com.jaegokeeper.board.model.Board;
import com.jaegokeeper.board.service.BoardService;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.exception.ErrorCode;
import com.jaegokeeper.image.service.ImageService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
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
}
