package com.jaegokeeper.hwan.item.controller;

import com.jaegokeeper.hwan.item.dto.request.ItemCreateRequest;
import com.jaegokeeper.hwan.item.dto.request.ItemPageRequest;
import com.jaegokeeper.hwan.item.dto.request.ItemUpdateRequest;
import com.jaegokeeper.hwan.item.dto.response.ItemCreateResponse;
import com.jaegokeeper.hwan.item.dto.response.ItemDetailResponse;
import com.jaegokeeper.hwan.item.dto.response.ItemListResponse;
import com.jaegokeeper.hwan.item.dto.response.ItemPageResponse;
import com.jaegokeeper.hwan.item.service.ItemService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
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

    // 생성
    @ApiOperation(value = "아이템 생성", notes = "매장(storeId)에 아이템을 등록합니다.")
    @PostMapping
    public ResponseEntity<ItemCreateResponse> createItem(
            @PathVariable Integer storeId,
            @Valid @ModelAttribute ItemCreateRequest dto) {
        Integer itemId = itemService.createItem(storeId, dto);
        return ResponseEntity.ok(new ItemCreateResponse(itemId));
    }

    // 조회
    @ApiOperation(value = "아이템 목록 조회", notes = "필터/페이지 조건으로 아이템 목록을 조회합니다.")
    @GetMapping
    public ResponseEntity<ItemPageResponse<ItemListResponse>> getItems(
            @PathVariable Integer storeId,
            @Valid @ModelAttribute ItemPageRequest dto
    ) {
        return ResponseEntity.ok(itemService.getItemList(storeId, dto));
    }

    // 상세 조회
    @ApiOperation(value = "아이템 상세 조회", notes = "itemId로 아이템 상세 조회합니다.")
    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDetailResponse> getItemDetail(
            @PathVariable Integer storeId,
            @PathVariable Integer itemId
    ) {
        return ResponseEntity.ok(itemService.getItemDetail(storeId, itemId));
    }

    // 수정
    @ApiOperation(value = "아이템 수정", notes = "itemId의 아이템 정보를 수정합니다.")
    @PutMapping("/{itemId}")
    public ResponseEntity<Void> modifyItem(
            @PathVariable Integer storeId,
            @PathVariable Integer itemId,
            @Valid @RequestBody ItemUpdateRequest dto
    ) {
        itemService.updateItem(storeId,itemId,dto);
        return ResponseEntity.noContent().build();
    }

    // 삭제
    @ApiOperation(value = "아이템 삭제", notes = "itemId의 아이템을 삭제합니다.")
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable Integer storeId,
            @PathVariable Integer itemId
    ) {
        itemService.softDeleteItem(storeId, itemId);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(value = "아이템 즐겨찾기 토글", notes = "아이템의 즐겨찾기 상태를 토글합니다.")
    @PatchMapping("/{itemId}/pin")
    public ResponseEntity<Void> toggleIsPinned(
            @PathVariable Integer storeId,
            @PathVariable Integer itemId
    ) {
        itemService.toggleItemPin(storeId, itemId);
        return ResponseEntity.noContent().build();
    }

}
