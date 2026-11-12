package com.jaegokeeper.item.service;

import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.common.dto.PageResponse;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.stock.dto.StockAndBufferUpdate;
import com.jaegokeeper.stock.service.StockService;
import com.jaegokeeper.image.dto.ImageInfoDTO;
import com.jaegokeeper.image.service.ImageService;
import com.jaegokeeper.item.model.Item;
import com.jaegokeeper.item.dto.ItemUpdateParam;
import com.jaegokeeper.item.dto.request.ItemCreateRequest;
import com.jaegokeeper.item.dto.request.ItemPageRequest;
import com.jaegokeeper.item.dto.request.ItemUpdateRequest;
import com.jaegokeeper.item.dto.response.ItemDetailResponse;
import com.jaegokeeper.item.dto.response.ItemListResponse;
import com.jaegokeeper.item.enums.ItemFilter;
import com.jaegokeeper.item.mapper.ItemMapper;
import com.jaegokeeper.store.mapper.StoreMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.jaegokeeper.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemMapper itemMapper;
    private final StoreMapper storeMapper;
    private final StockService stockService;
    private final ImageService imgService;

    @Transactional
    public Integer createItem(LoginContext login, ItemCreateRequest dto) {
        int storeId = login.getStoreId();
        if (!storeMapper.existsById(storeId)) {
            throw new BusinessException(STORE_NOT_FOUND);
        }

        ImageInfoDTO uploadedImage = imgService.uploadIfPresent(dto.getFile());
        Integer imageId = (uploadedImage != null) ? uploadedImage.getImageId() : null;
        try {
            Item item = Item.create(storeId, dto.getItemName(), imageId);

            int insertedItem = itemMapper.insertItem(item);
            if (insertedItem != 1) {
                throw new BusinessException(INTERNAL_ERROR);
            }

            stockService.initStock(item.getItemId(), dto.getStockAmount());

            return item.getItemId();
        } catch (Exception e) {
            if (uploadedImage != null) imgService.deleteImageFile(uploadedImage.getImagePath());
            if (e instanceof BusinessException) throw (BusinessException) e;
            log.error("[ITEM_CREATE] 아이템 생성 실패 storeId={}", storeId, e);
            throw new BusinessException(INTERNAL_ERROR, e);
        }
    }

    @Transactional
    public void softDeleteItem(LoginContext login, Integer itemId) {
        int updated = itemMapper.softDeleteItem(login.getStoreId(), itemId);
        if (updated != 1) {
            throw new BusinessException(ITEM_NOT_FOUND);
        }
    }

    @Transactional(readOnly = true)
    public PageResponse<ItemListResponse> getItemList(LoginContext login, ItemPageRequest dto) {
        int storeId = login.getStoreId();
        int pageNum = dto.getPageValue();
        int pageSize = dto.getSizeValue();
        String keyword = dto.getKeywordValue();
        ItemFilter filter = dto.getFilter();
        boolean excludeZero = dto.getExcludeZeroValue();

        int offset = (pageNum - 1) * pageSize;
        int totalElements = itemMapper.countItemList(storeId, filter, keyword, excludeZero);
        List<ItemListResponse> content = itemMapper.findItemList(storeId, filter, keyword, excludeZero, offset, pageSize);

        return PageResponse.of(content, pageNum, pageSize, totalElements);
    }

    @Transactional(readOnly = true)
    public ItemDetailResponse getItemDetail(LoginContext login, Integer itemId) {
        ItemDetailResponse dto = itemMapper.findItemDetail(login.getStoreId(), itemId);
        if (dto == null) {
            throw new BusinessException(ITEM_NOT_FOUND);
        }
        return dto;
    }

    @Transactional
    public void updateItem(LoginContext login, Integer itemId, ItemUpdateRequest dto) {
        int storeId = login.getStoreId();
        boolean wantsRemove = Boolean.TRUE.equals(dto.getRemoveImage());
        ImageInfoDTO uploadedImage = imgService.uploadForUpdate(dto.getFile(), dto.getRemoveImage());
        Integer newImageId = (uploadedImage != null) ? uploadedImage.getImageId() : null;

        try {
            ItemUpdateParam updateItem = new ItemUpdateParam(
                    dto.getItemName(),
                    dto.getIsPinned(),
                    newImageId,
                    wantsRemove ? true : null
            );

            int itemUpdated = itemMapper.updateItem(storeId, itemId, updateItem);
            if (itemUpdated != 1) {
                throw new BusinessException(ITEM_NOT_FOUND);
            }

            if (dto.getTargetAmount() != null || dto.getBufferAmount() != null) {
                StockAndBufferUpdate stockUpdate = new StockAndBufferUpdate(dto.getTargetAmount(), dto.getBufferAmount());
                stockService.updateStockAndBuffer(login, itemId, stockUpdate);
            }
        } catch (Exception e) {
            if (uploadedImage != null) imgService.deleteImageFile(uploadedImage.getImagePath());
            throw e;
        }
    }

    @Transactional
    public void toggleItemPin(LoginContext login, Integer itemId) {
        int updated = itemMapper.togglePin(login.getStoreId(), itemId);
        if (updated == 0) {
            throw new BusinessException(ITEM_NOT_FOUND);
        }
    }

}
