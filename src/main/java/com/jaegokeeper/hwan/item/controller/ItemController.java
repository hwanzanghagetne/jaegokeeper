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

    @PostMapping
    public ResponseEntity<ItemCreateResponseDTO> createItem(@Valid @RequestBody ItemCreateRequestDTO itemCreateRequestDTO) {


        Integer itemId = itemService.createItem(itemCreateRequestDTO);

        return ResponseEntity
                .status(201)
                .body(new ItemCreateResponseDTO(itemId));
    }

    @GetMapping
    public ResponseEntity<PageResponseDTO<ItemListDTO>> getItems(
            @Valid @ModelAttribute ItemPageRequestDTO dto
    ) {
        return ResponseEntity.ok(itemService.getItemList(dto));
    }

    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDetailDTO> getItemDetail(
            @RequestParam Integer storeId,
            @PathVariable Integer itemId
    ) {
        ItemDetailDTO dto = itemService.getItemDetail(storeId, itemId);
        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/{itemId}")
    public ResponseEntity<Void> modifyItem(
            @RequestParam Integer storeId,
            @PathVariable Integer itemId,
            @Valid @RequestBody ItemModifyRequestDTO dto
    ) {
        dto.setStoreId(storeId);
        dto.setItemId(itemId);
        itemService.modifyItem( dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @RequestParam Integer storeId,
            @PathVariable Integer itemId
    ) {
        itemService.softDeleteItem(storeId, itemId);
        return ResponseEntity.noContent().build();
    }
}
