package com.jaegokeeper.hwan.item.service;

import com.jaegokeeper.hwan.buffer.mapper.BufferMapper;
import com.jaegokeeper.hwan.exception.NotFoundException;
import com.jaegokeeper.hwan.item.domain.Item;
import com.jaegokeeper.hwan.item.dto.*;
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

import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {


    private final ItemMapper itemMapper;
    private final StockMapper stockMapper;
    private final StoreMapper storeMapper;
    private final StockService stockService;
    private final BufferMapper bufferMapper;


    //아이템 생성
    @Transactional
    @Override
    public ItemCreateResponseDTO createItem(Integer storeId, ItemCreateRequestDTO dto) {
        int count = storeMapper.countById(storeId);
        if (count == 0) {
            throw new NotFoundException("존재하지 않는 매장입니다.");
        }

        Item item = Item.of(storeId, dto.getItemName(), dto.getImageId());

        int insertedItem = itemMapper.insertItem(item);
        if (insertedItem != 1) {throw new IllegalStateException("아이템 등록 실패");}

        Stock stock = Stock.of(item.getItemId(), dto.getStockAmount());

        int insertedStock = stockMapper.insertStock(stock);
        if (insertedStock != 1) {throw new IllegalStateException("stock 생성 실패");}

        int bufferInserted = bufferMapper.insertBuffer(item.getItemId(), 0);
        if (bufferInserted != 1) {throw new IllegalStateException("safe 생성 실패");}

        return new ItemCreateResponseDTO(item.getItemId(), stock.getStockId());
    }

    @Override
    public void softDeleteItem(Integer storeId, Integer itemId) {
        int updated = itemMapper.softDeleteItem(storeId, itemId);
        if (updated != 1) {
            throw new IllegalStateException("삭제 실패");
        }
    }

    //아이템 리스트
    @Override
    public PageResponseDTO<ItemListDTO> getItemList(Integer storeId, ItemPageRequestDTO dto) {

        int pageNum = dto.getPageValue();
        int pageSize = dto.getSizeValue();
        String keyword = dto.getKeywordValue();
        ItemFilter filter = dto.getFilter();
        boolean excludeZero = dto.isExcludeZero();


        int offset = (pageNum - 1) * pageSize;

        int totalElements = itemMapper.countItemList(storeId,filter,keyword,excludeZero);
        List<ItemListDTO> content = itemMapper.findItemList(storeId, filter,keyword,excludeZero, offset, pageSize);
        int totalPages = (int) Math.ceil(((double) totalElements / pageSize));

        return new PageResponseDTO<>(content, pageNum, pageSize, totalElements, totalPages);
    }

    //아이템 상세 조회
    @Override
    public ItemDetailDTO getItemDetail(Integer storeId, Integer itemId) {
        ItemDetailDTO dto = itemMapper.getItemDetail(storeId, itemId);
        if (dto == null) {
            throw new NotFoundException("존재하지 않는 아이템입니다.");
        }
        return dto;
    }

    @Transactional
    @Override
    public void modifyItem( Integer storeId, Integer itemId,ItemModifyRequestDTO dto) {
        Integer stockId = stockMapper.findStockIdByItem(itemId);
        if (stockId == null) {
            throw new NotFoundException("재고 없음");
        }

        // item 수정
        int itemUpdated = itemMapper.updateItem(storeId, itemId, dto.getItemName(),dto.getIsPinned(),dto.getImageId());
        if (itemUpdated != 1) {throw new IllegalStateException("아이템 수정 실패");}

        // 재고 수정
        StockAdjustRequestDTO stockAdjustRequestDTO = new StockAdjustRequestDTO(dto.getTargetAmount(), dto.getBufferAmount());
        stockService.adjustStock(itemId,stockAdjustRequestDTO);
    }

}
