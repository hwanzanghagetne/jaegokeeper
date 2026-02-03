package com.jaegokeeper.hwan.item.service;

import com.jaegokeeper.ddan.img.service.ImgService;
import com.jaegokeeper.hwan.buffer.mapper.BufferMapper;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.hwan.item.domain.Item;
import com.jaegokeeper.hwan.item.dto.request.ItemCreateRequest;
import com.jaegokeeper.hwan.item.dto.request.ItemPageRequest;
import com.jaegokeeper.hwan.item.dto.request.ItemUpdateRequest;
import com.jaegokeeper.hwan.item.dto.response.ItemDetailResponse;
import com.jaegokeeper.hwan.item.dto.response.ItemListResponse;
import com.jaegokeeper.hwan.item.dto.response.ItemPageResponse;
import com.jaegokeeper.hwan.item.mapper.ItemMapper;
import com.jaegokeeper.hwan.item.mapper.StoreMapper;
import com.jaegokeeper.hwan.stock.dto.StockAdjustRequestDTO;
import com.jaegokeeper.hwan.stock.mapper.StockMapper;
import com.jaegokeeper.hwan.stock.service.StockService;
import com.jaegokeeper.hwan.stock.domain.Stock;
import com.jaegokeeper.hwan.item.enums.ItemFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;

import static com.jaegokeeper.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {


    private final ItemMapper itemMapper;
    private final StockMapper stockMapper;
    private final StoreMapper storeMapper;
    private final StockService stockService;
    private final BufferMapper bufferMapper;
    private final ImgService imgService;


    // 생성
    @Transactional
    @Override
    public Integer createItem(Integer storeId, ItemCreateRequest dto) {
        int count = storeMapper.countById(storeId);
        if (count == 0) {
            throw new BusinessException(STORE_NOT_FOUND);
        }

        Integer imageId = null;
        try {
            if (dto.getFile() != null && !dto.getFile().isEmpty()) {
                imageId = imgService.uploadImg(dto);
            }
        } catch (IOException e) {
            throw new BusinessException(IMAGE_UPLOAD_FAILED, e);
        }
        Item item = Item.create(storeId, dto.getItemName(), imageId);

        int insertedItem = itemMapper.insertItem(item);
        if (insertedItem != 1) {
            throw new BusinessException(INTERNAL_ERROR);}

        Stock stock = Stock.of(item.getItemId(), dto.getStockAmount());

        int insertedStock = stockMapper.insertStock(stock);
        if (insertedStock != 1) {throw new BusinessException(INTERNAL_ERROR);}

        int bufferInserted = bufferMapper.insertBuffer(item.getItemId(), 0);
        if (bufferInserted != 1) {throw new BusinessException(INTERNAL_ERROR);}

        return item.getItemId();
    }

    @Override
    public void softDeleteItem(Integer storeId, Integer itemId) {
        int updated = itemMapper.softDeleteItem(storeId, itemId);
        if (updated != 1) {
            throw new BusinessException(INTERNAL_ERROR);
        }
    }

    //아이템 리스트
    @Override
    public ItemPageResponse<ItemListResponse> getItemList(Integer storeId, ItemPageRequest dto) {

        int pageNum = dto.getPageValue();
        int pageSize = dto.getSizeValue();
        String keyword = dto.getKeywordValue();
        ItemFilter filter = dto.getFilter();
        boolean excludeZero = dto.isExcludeZero();


        int offset = (pageNum - 1) * pageSize;

        int totalElements = itemMapper.countItemList(storeId,filter,keyword,excludeZero);
        List<ItemListResponse> content = itemMapper.findItemList(storeId, filter,keyword,excludeZero, offset, pageSize);
        int totalPages = (totalElements + pageSize - 1) / pageSize;

        return new ItemPageResponse<>(content, pageNum, pageSize, totalElements, totalPages);
    }

    //아이템 상세 조회
    @Override
    public ItemDetailResponse getItemDetail(Integer storeId, Integer itemId) {
        ItemDetailResponse dto = itemMapper.findItemDetail(storeId, itemId);
        if (dto == null) {
            throw new BusinessException(ITEM_NOT_FOUND);
        }
        return dto;
    }

    @Transactional
    @Override
    public void updateItem(Integer storeId, Integer itemId, ItemUpdateRequest dto) {

        // 아이템 수정
        int itemUpdated = itemMapper.updateItem(storeId, itemId, dto.getItemName(),dto.getIsPinned(),dto.getImageId());
        if (itemUpdated != 1) {throw new BusinessException(INTERNAL_ERROR);}

        // 재고 수정
        StockAdjustRequestDTO stockAdjustRequestDTO = new StockAdjustRequestDTO(dto.getTargetAmount(), dto.getBufferAmount());
        stockService.adjustStock(itemId,stockAdjustRequestDTO);
    }

    @Transactional
    @Override
    public void toggleItemPin(Integer storeId, Integer itemId) {
        int updated = itemMapper.togglePin(storeId, itemId);
        if (updated == 0) {
            throw new BusinessException(ITEM_NOT_FOUND);
        }
    }
}
