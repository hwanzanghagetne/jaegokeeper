package com.jaegokeeper.hwan.item.service;

import com.jaegokeeper.hwan.exception.NotFoundException;
import com.jaegokeeper.hwan.item.domain.Item;
import com.jaegokeeper.hwan.item.dto.*;
import com.jaegokeeper.hwan.item.mapper.ItemMapper;
import com.jaegokeeper.hwan.item.mapper.StoreMapper;
import com.jaegokeeper.hwan.stock.dto.StockAdjustRequestDTO;
import com.jaegokeeper.hwan.stock.mapper.SafeMapper;
import com.jaegokeeper.hwan.stock.mapper.StockMapper;
import com.jaegokeeper.hwan.stock.service.StockService;
import com.jaegokeeper.hwan.stock.domain.Stock;
import com.jaegokeeper.hwan.item.enums.ItemFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {


    private final ItemMapper itemMapper;
    private final StockMapper stockMapper;
    private final StoreMapper storeMapper;
    private final SafeMapper safeMapper;
    private final StockService stockService;


    //아이템 생성
    @Transactional
    @Override
    public int createItem(ItemCreateRequestDTO itemCreateRequestDTO) {

        String itemName = itemCreateRequestDTO.getItemName();
        Integer storeId = itemCreateRequestDTO.getStoreId();
        Integer quantity = itemCreateRequestDTO.getQuantity();

        int count = storeMapper.countById(storeId);
        if (count == 0) {
            throw new NotFoundException("존재하지 않는 매장입니다.");
        }

        Item item = new Item();
        item.setStoreId(storeId);
        item.setItemName(itemName);
        item.setImageId(itemCreateRequestDTO.getImageId());

        int itemInserted = itemMapper.insertItem(item);
        if (itemInserted != 1) {
            throw new IllegalStateException("아이템 등록 실패");
        }

        Stock stock = new Stock();
        stock.setItemId(item.getItemId());
        stock.setStoreId(storeId);
        stock.setQuantity(quantity);

        int stockInserted = stockMapper.insertStock(stock);
        if (stockInserted != 1
        ) {
            throw new IllegalStateException("stock 생성 실패");
        }

        int safeInserted = safeMapper.insertSafe(stock.getStockId(), 0);
        if (safeInserted != 1) {
            throw new IllegalStateException("safe 생성 실패");
        }

        return item.getItemId();
    }

    @Override
    public void softDeleteItem(Integer storeId, Integer itemId) {
        int updated = itemMapper.softDeleteItem(storeId, itemId);
        if (updated != 1) {
            throw new IllegalStateException("삭제 실패");
        }
    }

    //아이템 리스트
    @Transactional
    @Override
    public PageResponseDTO<ItemListDTO> getItemList(ItemPageRequestDTO dto) {

        Integer storeId = dto.getStoreId();
        int pageNum = dto.getPageValue();
        int pageSize = dto.getSizeValue();
        String keyword = dto.getKeywordValue();
        ItemFilter filter = dto.getFilter();


        int offset = (pageNum - 1) * pageSize;

        int totalElements = itemMapper.countItemList(storeId,filter,keyword);
        List<ItemListDTO> content = itemMapper.findItemList(storeId, filter,keyword, offset, pageSize);
        int totalPages = (int) Math.ceil(((double) totalElements / pageSize));

        return new PageResponseDTO<>(content, pageNum, pageSize, totalElements, totalPages);
    }

    //아이템 상세 조회
    @Transactional
    @Override
    public ItemDetailDTO getItemDetail(Integer storeId, Integer itemId) {

        if (storeId == null || storeId <= 0) {
            throw new IllegalArgumentException("storeId는 1 이상이어야 합니다.");
        }
        if (itemId == null || itemId <= 0) {
            throw new IllegalArgumentException("itemId는 1 이상이어야 합니다.");
        }
        ItemDetailDTO dto = itemMapper.findItemDetail(storeId, itemId);

        if (dto == null) {
            throw new NotFoundException("존재하지 않는 아이템입니다.");
        }

        return dto;
    }

    @Transactional
    @Override
    public void modifyItem( Integer storeId, Integer itemId,ItemModifyRequestDTO dto) {
        Integer stockId = stockMapper.findStockIdByStoreAndItem(storeId, itemId);
        if (stockId == null) {
            throw new NotFoundException("재고 없음");
        }

        // item 수정
        int itemUpdated = itemMapper.updateItem(
                storeId, itemId,
                dto.getItemName(),
                dto.getImageId());
        if (itemUpdated != 1) {
            throw new IllegalStateException("아이템 수정 실패");
        }

        // 재고 수정
        StockAdjustRequestDTO stockAdjustRequestDTO = new StockAdjustRequestDTO(
                dto.getTargetQuantity(),
                dto.getSafeQuantity(),
                dto.getFavoriteYn());

        stockService.adjustStock(stockId,stockAdjustRequestDTO);
    }

}
