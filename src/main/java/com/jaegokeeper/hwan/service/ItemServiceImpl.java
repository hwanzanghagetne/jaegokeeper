package com.jaegokeeper.hwan.service;

import com.jaegokeeper.hwan.domain.Item;
import com.jaegokeeper.hwan.domain.Stock;
import com.jaegokeeper.hwan.domain.enums.StockHistoryType;
import com.jaegokeeper.hwan.dto.*;
import com.jaegokeeper.hwan.mapper.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.jaegokeeper.hwan.domain.enums.StockHistoryType.*;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {


    private final ItemMapper itemMapper;
    private final StockMapper stockMapper;
    private final StoreMapper storeMapper;
    private final SafeMapper safeMapper;
    private final StockHistoryMapper stockHistoryMapper;
    private final StockService stockService;


    //아이템 생성
    @Transactional
    @Override
    public Integer createItem(ItemCreateRequestDTO itemCreateRequestDTO) {

        String itemName = itemCreateRequestDTO.getItemName();
        Integer storeId = itemCreateRequestDTO.getStoreId();
        if (storeId <= 0) {
            throw new IllegalArgumentException("storeId는 1 이상이어야 합니다.");
        }

        int count = storeMapper.countById(storeId);
        if (count == 0) {
            throw new IllegalArgumentException("존재하지 않는 매장입니다.");
        }

        Integer quantity = itemCreateRequestDTO.getQuantity();
        if (quantity < 0) {
            throw new IllegalArgumentException("quantity는 0 이상이어야 합니다.");
        }

        Item item = new Item();
        item.setStoreId(storeId);
        item.setItemName(itemName);
        item.setImageId(itemCreateRequestDTO.getImageId());

        itemMapper.insertItem(item);


        Stock stock = new Stock();
        stock.setItemId(item.getItemId());
        stock.setStoreId(storeId);
        stock.setQuantity(quantity);

        stockMapper.insertStock(stock);
        int inserted = safeMapper.insertSafe(stock.getStockId(), 0);
        if (inserted != 1) {
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
    public PageResponseDTO<ItemListDTO> getItemList(Integer storeId, int page, int size, List<String> filters, String keyword) {

        if (storeId == null || storeId <= 0) {
            throw new IllegalArgumentException("storeId는 필수이며 1 이상이어야 합니다.");
        }
        if (page <= 0) {
            throw new IllegalArgumentException("page는 1 이상이어야 합니다.");
        }
        if (size <= 0 || size > 50) {
            throw new IllegalArgumentException("size는 1 이상 50 이하만 허용됩니다.");
        }

        //추후 enum으로 빼면 좋을까?
        if (filters != null) {
            for (String f : filters) {
                if (!f.equals("FAVORITE") && !f.equals("ZERO_STOCK")) {
                    throw new IllegalArgumentException("filters에는 FAVORITE 또는 ZERO_STOCK 만 허용됩니다.");
                }
            }
        }

        if (keyword != null && keyword.isBlank()) {
            keyword = null;
        } else if (keyword != null) {
            keyword = keyword.trim();
        }

        int offset = (page - 1) * size;
        long totalElements = itemMapper.countItemList(storeId,filters,keyword);
        List<ItemListDTO> content = itemMapper.findItemList(storeId, filters,keyword, offset, size);
        int totalPages = (int) Math.ceil(((double) totalElements / size));

        return new PageResponseDTO<>(content, page, size, totalElements, totalPages);

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
            throw new IllegalArgumentException("존재하지 않는 아이템입니다.");
        }

        return dto;
    }

    @Transactional
    @Override
    public void modifyItem(Integer storeId, Integer itemId, ItemModifyRequestDTO dto) {
        Integer realStockId = stockMapper.findStockIdByStoreAndItem(storeId, itemId);
        if (realStockId == null) {
            throw new IllegalStateException("재고 없음");
        }
        if (!realStockId.equals(dto.getStockId())) {
            throw new IllegalArgumentException("잘못된 stockId");
        }
        // item 수정

        int itemUpdated = itemMapper.updateItem(storeId, itemId, dto.getItemName(), dto.getImageId());
        if (itemUpdated != 1) {
            throw new IllegalStateException("아이템 수정 실패");
        }

        // 재고 수정
        Integer stockId = dto.getStockId();
        Integer targetQuantity = dto.getTargetQuantity();
        Integer safeQuantity = dto.getSafeQuantity();
        Boolean favoriteYn = dto.getFavoriteYn();

        stockService.adjustStock(stockId,targetQuantity,safeQuantity,favoriteYn);
    }

}
