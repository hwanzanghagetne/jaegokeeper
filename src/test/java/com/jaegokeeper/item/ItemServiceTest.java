package com.jaegokeeper.item;

import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.exception.ErrorCode;
import com.jaegokeeper.image.dto.ImageInfoDTO;
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
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
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

    private LoginContext login;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        login = new LoginContext(10, 1, "tester", "LOCAL");
    }


    @Test
    public void 아이템생성_매장없음_예외() {
        ItemCreateRequest req = new ItemCreateRequest();
        req.setItemName("치킨");
        req.setStockAmount(10);

        when(storeMapper.existsById(1)).thenReturn(false);

        BusinessException e = assertThrows(BusinessException.class,
                () -> itemService.createItem(login, req));

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
                () -> itemService.createItem(login, req));

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

        Integer itemId = itemService.createItem(login, req);

        assertEquals(Integer.valueOf(100), itemId);
        verify(stockService).initStock(100, 10);
    }

    @Test
    public void 아이템삭제_대상없음_예외() {
        when(itemMapper.softDeleteItem(1, 5)).thenReturn(0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> itemService.softDeleteItem(login, 5));

        assertEquals(ErrorCode.ITEM_NOT_FOUND, e.getErrorCode());
    }

    @Test
    public void 아이템상세조회_없음_예외() {
        when(itemMapper.findItemDetail(1, 5)).thenReturn(null);

        BusinessException e = assertThrows(BusinessException.class,
                () -> itemService.getItemDetail(login, 5));

        assertEquals(ErrorCode.ITEM_NOT_FOUND, e.getErrorCode());
    }

    @Test
    public void 즐겨찾기토글_대상없음_예외() {
        when(itemMapper.togglePin(1, 5)).thenReturn(0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> itemService.toggleItemPin(login, 5));

        assertEquals(ErrorCode.ITEM_NOT_FOUND, e.getErrorCode());
    }

    private ImageInfoDTO uploaded(int imageId, String path) {
        ImageInfoDTO info = new ImageInfoDTO();
        info.setImageId(imageId);
        info.setImagePath(path);
        return info;
    }

    @Test
    public void 아이템생성_업로드한_이미지_ID로_저장() {
        ItemCreateRequest req = new ItemCreateRequest();
        req.setItemName("치킨");
        req.setStockAmount(10);
        req.setFile(new MockMultipartFile("file", "a.png", "image/png", new byte[]{1}));
        when(storeMapper.existsById(1)).thenReturn(true);
        when(imgService.uploadIfPresent(req.getFile())).thenReturn(uploaded(9, "2026/11/10/a.png"));
        doAnswer(invocation -> {
            Item item = invocation.getArgument(0);
            item.setItemId(100);
            return 1;
        }).when(itemMapper).insertItem(any(Item.class));

        itemService.createItem(login, req);

        ArgumentCaptor<Item> captor = ArgumentCaptor.forClass(Item.class);
        verify(itemMapper).insertItem(captor.capture());
        assertEquals(Integer.valueOf(9), captor.getValue().getImageId());
    }

    @Test
    public void 아이템생성_저장_실패시_올린_이미지_삭제() {
        ItemCreateRequest req = new ItemCreateRequest();
        req.setItemName("치킨");
        req.setStockAmount(10);
        when(storeMapper.existsById(1)).thenReturn(true);
        when(imgService.uploadIfPresent(any())).thenReturn(uploaded(9, "2026/11/10/a.png"));
        when(itemMapper.insertItem(any(Item.class))).thenReturn(0);

        assertThrows(BusinessException.class, () -> itemService.createItem(login, req));

        verify(imgService).deleteImageFile("2026/11/10/a.png");
    }

    @Test
    public void 아이템수정_갱신_실패시_새로_올린_이미지_삭제() {
        ItemUpdateRequest req = new ItemUpdateRequest();
        when(imgService.uploadForUpdate(any(), any())).thenReturn(uploaded(9, "2026/11/10/b.png"));
        when(itemMapper.updateItem(any(Integer.class), any(Integer.class), any())).thenReturn(0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> itemService.updateItem(login, 5, req));

        assertEquals(ErrorCode.ITEM_NOT_FOUND, e.getErrorCode());
        verify(imgService).deleteImageFile("2026/11/10/b.png");
    }

    @Test
    public void 아이템수정_대상없음_예외() {
        ItemUpdateRequest req = new ItemUpdateRequest();
        req.setItemName("변경된 이름");

        when(itemMapper.updateItem(any(Integer.class), any(Integer.class), any())).thenReturn(0);

        BusinessException e = assertThrows(BusinessException.class,
                () -> itemService.updateItem(login, 5, req));

        assertEquals(ErrorCode.ITEM_NOT_FOUND, e.getErrorCode());
    }
}
