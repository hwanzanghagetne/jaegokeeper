package com.jaegokeeper.item;

import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.auth.utils.StoreAccessValidator;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.exception.ErrorCode;
import com.jaegokeeper.image.service.ImageService;
import com.jaegokeeper.item.dto.request.ItemCreateRequest;
import com.jaegokeeper.item.dto.request.ItemUpdateRequest;
import com.jaegokeeper.item.mapper.ItemMapper;
import com.jaegokeeper.item.model.Item;
import com.jaegokeeper.item.service.ItemService;
import com.jaegokeeper.stock.service.StockService;
import com.jaegokeeper.store.mapper.StoreMapper;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ItemServiceTest {

    @InjectMocks
    private ItemService itemService;

    @Mock
    private ItemMapper itemMapper;

    @Mock
    private StoreMapper storeMapper;

    @Mock
    private StockService stockService;

    @Mock
    private ImageService imgService;

    @Mock
    private StoreAccessValidator storeAccessValidator;

    private LoginContext login;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        login = new LoginContext(10, 1, "tester", "LOCAL");
    }

    @Test
    public void 아이템생성_본인매장아님_예외() {
        ItemCreateRequest req = new ItemCreateRequest();
        req.setItemName("치킨");
        req.setStockAmount(10);

        doThrow(new BusinessException(ErrorCode.FORBIDDEN))
                .when(storeAccessValidator).validate(login, 99);

        BusinessException e = assertThrows(BusinessException.class,
                () -> itemService.createItem(login, 99, req));

        assertEquals(ErrorCode.FORBIDDEN, e.getErrorCode());
    }

    @Test
    public void 아이템생성_매장없음_예외() {
        ItemCreateRequest req = new ItemCreateRequest();
        req.setItemName("치킨");
        req.setStockAmount(10);

        when(storeMapper.existsById(1)).thenReturn(false);

        BusinessException e = assertThrows(BusinessException.class,
                () -> itemService.createItem(login, 1, req));

        assertEquals(ErrorCode.STORE_NOT_FOUND, e.getErrorCode());
    }

    @Test
    public void 아이템생성_insert실패_예외() {
        ItemCreateRequest req = new ItemCreateRequest();
        req.setItemName("치킨");
        req.setStockAmount(10);

        when(storeMapper.existsById(1)).thenReturn(true);
        when(itemMapper.insertItem(any(Item.class))).thenReturn(0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> itemService.createItem(login, 1, req));

        assertEquals(ErrorCode.INTERNAL_ERROR, e.getErrorCode());
    }

    @Test
    public void 아이템생성_성공시_재고초기화_호출() {
        ItemCreateRequest req = new ItemCreateRequest();
        req.setItemName("치킨");
        req.setStockAmount(10);

        when(storeMapper.existsById(1)).thenReturn(true);
        doAnswer(invocation -> {
            Item item = invocation.getArgument(0);
            item.setItemId(100);
            return 1;
        }).when(itemMapper).insertItem(any(Item.class));

        Integer itemId = itemService.createItem(login, 1, req);

        assertEquals(Integer.valueOf(100), itemId);
        verify(stockService).initStock(100, 10);
    }

    @Test
    public void 아이템삭제_대상없음_예외() {
        when(itemMapper.softDeleteItem(1, 5)).thenReturn(0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> itemService.softDeleteItem(login, 1, 5));

        assertEquals(ErrorCode.ITEM_NOT_FOUND, e.getErrorCode());
    }

    @Test
    public void 아이템상세조회_없음_예외() {
        when(itemMapper.findItemDetail(1, 5)).thenReturn(null);

        BusinessException e = assertThrows(BusinessException.class,
                () -> itemService.getItemDetail(login, 1, 5));

        assertEquals(ErrorCode.ITEM_NOT_FOUND, e.getErrorCode());
    }

    @Test
    public void 즐겨찾기토글_대상없음_예외() {
        when(itemMapper.togglePin(1, 5)).thenReturn(0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> itemService.toggleItemPin(login, 1, 5));

        assertEquals(ErrorCode.ITEM_NOT_FOUND, e.getErrorCode());
    }

    @Test
    public void 아이템수정_이미지제거와파일동시요청_예외() {
        MultipartFile file = org.mockito.Mockito.mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);

        ItemUpdateRequest req = new ItemUpdateRequest();
        req.setRemoveImage(true);
        req.setFile(file);

        BusinessException e = assertThrows(BusinessException.class,
                () -> itemService.updateItem(login, 1, 5, req));

        assertEquals(ErrorCode.IMAGE_UPDATE_CONFLICT, e.getErrorCode());
    }

    @Test
    public void 아이템수정_대상없음_예외() {
        ItemUpdateRequest req = new ItemUpdateRequest();
        req.setItemName("변경된 이름");

        when(itemMapper.updateItem(any(Integer.class), any(Integer.class), any())).thenReturn(0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> itemService.updateItem(login, 1, 5, req));

        assertEquals(ErrorCode.ITEM_NOT_FOUND, e.getErrorCode());
    }
}
