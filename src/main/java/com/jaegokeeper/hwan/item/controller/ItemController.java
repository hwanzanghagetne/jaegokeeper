package com.jaegokeeper.hwan.item.controller;

import com.jaegokeeper.hwan.item.dto.*;
import com.jaegokeeper.hwan.item.service.ItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    // 아이템 생성
    @PostMapping
    public ResponseEntity<ItemCreateResponseDTO> createItem(@Valid @RequestBody ItemCreateRequestDTO itemCreateRequestDTO) {


        Integer itemId = itemService.createItem(itemCreateRequestDTO);

        return ResponseEntity
                .status(201)
                .body(new ItemCreateResponseDTO(itemId));
    }

    // 아이템 조회
    @GetMapping
    public ResponseEntity<PageResponseDTO<ItemListDTO>> getItems(
            @Valid @ModelAttribute ItemPageRequestDTO dto
    ) {
        return ResponseEntity.ok(itemService.getItemList(dto));
    }

    // 아이템 상세 조회
    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDetailDTO> getItemDetail(
            @RequestParam Integer storeId,
            @PathVariable Integer itemId
    ) {
        ItemDetailDTO dto = itemService.getItemDetail(storeId, itemId);
        return ResponseEntity.ok(dto);
    }

    // 아이템 수정
    @PatchMapping("/{itemId}")
    public ResponseEntity<Void> modifyItem(
            @RequestParam Integer storeId,
            @PathVariable Integer itemId,
            @Valid @RequestBody ItemModifyRequestDTO dto
    ) {
        itemService.modifyItem(storeId,itemId,dto);
        return ResponseEntity.noContent().build();
    }

    // 아이템 삭제
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @RequestParam Integer storeId,
            @PathVariable Integer itemId
    ) {
        itemService.softDeleteItem(storeId, itemId);
        return ResponseEntity.noContent().build();
    }
}
