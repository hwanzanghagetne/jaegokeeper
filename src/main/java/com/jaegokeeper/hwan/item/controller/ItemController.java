package com.jaegokeeper.hwan.item.controller;

import com.jaegokeeper.hwan.item.dto.*;
import com.jaegokeeper.hwan.item.service.ItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Api(tags = "Item")
@RestController
@RequestMapping("/stores/{storeId}/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    // 아이템 생성
    @ApiOperation(value = "아이템 생성", notes = "매장(storeId)에 아이템을 등록합니다.")
    @PostMapping
    public ResponseEntity<ItemCreateResponseDTO> createItem(
            @PathVariable Integer storeId,
            @Valid @RequestBody ItemCreateRequestDTO dto) {

        return ResponseEntity.ok(itemService.createItem(storeId, dto));
    }

    // 아이템 조회
    @ApiOperation(value = "아이템 목록 조회", notes = "필터/페이지 조건으로 아이템 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<PageResponseDTO<ItemListDTO>> getItems(
            @PathVariable Integer storeId,
            @Valid @ModelAttribute ItemPageRequestDTO dto
    ) {
        return ResponseEntity.ok(itemService.getItemList(storeId, dto));
    }

    // 아이템 상세 조회
    @ApiOperation(value = "아이템 상세 조회", notes = "itemId로 아이템 상세 조회합니다.")
    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDetailDTO> getItemDetail(
            @PathVariable Integer storeId,
            @PathVariable Integer itemId
    ) {
        return ResponseEntity.ok(itemService.getItemDetail(storeId, itemId));
    }

    // 아이템 수정
    @ApiOperation(value = "아이템 수정", notes = "itemId의 아이템 정보를 수정합니다.")
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
    @ApiOperation(value = "아이템 삭제", notes = "itemId의 아이템을 삭제합니다.")
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable Integer storeId,
            @PathVariable Integer itemId
    ) {
        itemService.softDeleteItem(storeId, itemId);
        return ResponseEntity.noContent().build();
    }
}
