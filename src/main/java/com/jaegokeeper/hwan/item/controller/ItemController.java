package com.jaegokeeper.hwan.item.controller;

import com.jaegokeeper.hwan.item.dto.*;
import com.jaegokeeper.hwan.item.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/stores/{storeId}/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    // 아이템 생성
    @PostMapping
    public ResponseEntity<ItemCreateResponseDTO> createItem(
            @PathVariable Integer storeId,
            @Valid @RequestBody ItemCreateRequestDTO dto) {

        return ResponseEntity.ok(itemService.createItem(storeId, dto));
    }

    // 아이템 조회
    @GetMapping
    public ResponseEntity<PageResponseDTO<ItemListDTO>> getItems(
            @PathVariable Integer storeId,
            @Valid @ModelAttribute ItemPageRequestDTO dto
    ) {
        return ResponseEntity.ok(itemService.getItemList(storeId, dto));
    }

    // 아이템 상세 조회
    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDetailDTO> getItemDetail(
            @PathVariable Integer storeId,
            @PathVariable Integer itemId
    ) {
        return ResponseEntity.ok(itemService.getItemDetail(storeId, itemId));
    }

    // 아이템 수정
    @PutMapping("/{itemId}")
    public ResponseEntity<Void> modifyItem(
            @PathVariable Integer storeId,
            @PathVariable Integer itemId,
            @Valid @RequestBody ItemModifyRequestDTO dto
    ) {
        itemService.modifyItem(storeId,itemId,dto);
        return ResponseEntity.noContent().build();
    }

    // 아이템 삭제
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable Integer storeId,
            @PathVariable Integer itemId
    ) {
        itemService.softDeleteItem(storeId, itemId);
        return ResponseEntity.noContent().build();
    }
}
