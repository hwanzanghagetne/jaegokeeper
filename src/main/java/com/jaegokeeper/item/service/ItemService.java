package com.jaegokeeper.item.service;

import com.jaegokeeper.common.dto.PageResponse;
import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.stock.mapper.BufferMapper;
import com.jaegokeeper.stock.domain.Stock;
import com.jaegokeeper.stock.dto.StockAdjustRequest;
import com.jaegokeeper.stock.mapper.StockMapper;
import com.jaegokeeper.stock.service.StockService;
import com.jaegokeeper.image.dto.ImageInfoDTO;
import com.jaegokeeper.image.service.ImageService;
import com.jaegokeeper.item.domain.Item;
import com.jaegokeeper.item.dto.ItemUpdateParamImg;
import com.jaegokeeper.item.dto.request.ItemCreateRequest;
import com.jaegokeeper.item.dto.request.ItemPageRequest;
import com.jaegokeeper.item.dto.request.ItemUpdateRequest;
import com.jaegokeeper.item.dto.response.ItemDetailResponse;
import com.jaegokeeper.item.dto.response.ItemListResponse;
import com.jaegokeeper.item.enums.ItemFilter;
import com.jaegokeeper.item.mapper.ItemMapper;
import com.jaegokeeper.store.mapper.StoreMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

import static com.jaegokeeper.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemMapper itemMapper;
    private final StockMapper stockMapper;
    private final StoreMapper storeMapper;
    private final StockService stockService;
    private final BufferMapper bufferMapper;
    private final ImageService imgService;

    @Transactional
    public Integer createItem(Integer storeId, ItemCreateRequest dto) {
        if (!storeMapper.existsById(storeId)) {
            throw new BusinessException(STORE_NOT_FOUND);
        }

        Integer imageId = uploadImageIfPresent(dto);
        Item item = Item.create(storeId, dto.getItemName(), imageId);

        int insertedItem = itemMapper.insertItem(item);
        if (insertedItem != 1) {
            throw new BusinessException(INTERNAL_ERROR);
        }

        Stock stock = Stock.create(item.getItemId(), dto.getStockAmount());
        int insertedStock = stockMapper.insertStock(stock);
        if (insertedStock != 1) {
            throw new BusinessException(INTERNAL_ERROR);
        }

        int bufferInserted = bufferMapper.insertBuffer(item.getItemId(), 0);
        if (bufferInserted != 1) {
            throw new BusinessException(INTERNAL_ERROR);
        }

        return item.getItemId();
    }

    @Transactional
    public void softDeleteItem(Integer storeId, Integer itemId) {
        int updated = itemMapper.softDeleteItem(storeId, itemId);
        if (updated != 1) {
            throw new BusinessException(ITEM_NOT_FOUND);
        }
    }

    public PageResponse<ItemListResponse> getItemList(Integer storeId, ItemPageRequest dto) {
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

    public ItemDetailResponse getItemDetail(Integer storeId, Integer itemId) {
        ItemDetailResponse dto = itemMapper.findItemDetail(storeId, itemId);
        if (dto == null) {
            throw new BusinessException(ITEM_NOT_FOUND);
        }
        return dto;
    }

    @Transactional
    public void updateItem(Integer storeId, Integer itemId, ItemUpdateRequest dto) {
        boolean wantsRemove = Boolean.TRUE.equals(dto.getRemoveImage());
        Integer newImageId = resolveImageIdForUpdate(dto.getFile(), dto.getRemoveImage(), dto);

        ItemUpdateParamImg updateItem = new ItemUpdateParamImg(
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
            StockAdjustRequest stockAdjustRequest = new StockAdjustRequest(dto.getTargetAmount(), dto.getBufferAmount());
            stockService.adjustStock(storeId, itemId, stockAdjustRequest);
        }
    }

    @Transactional
    public void toggleItemPin(Integer storeId, Integer itemId) {
        int updated = itemMapper.togglePin(storeId, itemId);
        if (updated == 0) {
            throw new BusinessException(ITEM_NOT_FOUND);
        }
    }

    private Integer uploadImageIfPresent(ImageInfoDTO dto) {
        if (dto.getFile() == null || dto.getFile().isEmpty()) return null;
        try {
            return imgService.uploadImg(dto);
        } catch (IOException e) {
            throw new BusinessException(IMAGE_UPLOAD_FAILED, e);
        }
    }

    private Integer resolveImageIdForUpdate(MultipartFile file, Boolean removeImage, ImageInfoDTO dto) {
        boolean hasFile = (file != null && !file.isEmpty());
        boolean wantsRemove = Boolean.TRUE.equals(removeImage);
        if (wantsRemove && hasFile) {
            throw new BusinessException(IMAGE_UPDATE_CONFLICT);
        }
        if (!hasFile) return null;
        try {
            return imgService.uploadImg(dto);
        } catch (IOException e) {
            throw new BusinessException(IMAGE_UPLOAD_FAILED, e);
        }
    }
}
