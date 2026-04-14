package com.jaegokeeper.item.controller;

import com.jaegokeeper.auth.annotation.LoginUser;
import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.common.dto.PageResponse;
import com.jaegokeeper.item.dto.request.ItemCreateRequest;
import com.jaegokeeper.item.dto.request.ItemPageRequest;
import com.jaegokeeper.item.dto.request.ItemUpdateRequest;
import com.jaegokeeper.item.dto.response.ItemCreateResponse;
import com.jaegokeeper.item.dto.response.ItemDetailResponse;
import com.jaegokeeper.item.dto.response.ItemListResponse;
import com.jaegokeeper.item.service.ItemService;
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

    @ApiOperation(value = "아이템 생성")
    @PostMapping
    public ResponseEntity<ItemCreateResponse> createItem(
            @PathVariable Integer storeId,
            @Valid @ModelAttribute ItemCreateRequest dto,
            @LoginUser LoginContext login) {
        Integer itemId = itemService.createItem(login, storeId, dto);
        return ResponseEntity.status(201).body(new ItemCreateResponse(itemId));
    }

    @ApiOperation(value = "아이템 목록 조회")
    @GetMapping
    public ResponseEntity<PageResponse<ItemListResponse>> getItems(
            @PathVariable Integer storeId,
            @Valid @ModelAttribute ItemPageRequest dto,
            @LoginUser LoginContext login) {
        return ResponseEntity.ok(itemService.getItemList(login, storeId, dto));
    }

    @ApiOperation(value = "아이템 상세 조회")
    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDetailResponse> getItemDetail(
            @PathVariable Integer storeId,
            @PathVariable Integer itemId,
            @LoginUser LoginContext login) {
        return ResponseEntity.ok(itemService.getItemDetail(login, storeId, itemId));
    }

    @ApiOperation(value = "아이템 수정")
    @PutMapping("/{itemId}")
    public ResponseEntity<Void> modifyItem(
            @PathVariable Integer storeId,
            @PathVariable Integer itemId,
            @Valid @ModelAttribute ItemUpdateRequest dto,
            @LoginUser LoginContext login) {
        itemService.updateItem(login, storeId, itemId, dto);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(value = "아이템 삭제")
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable Integer storeId,
            @PathVariable Integer itemId,
            @LoginUser LoginContext login) {
        itemService.softDeleteItem(login, storeId, itemId);
        return ResponseEntity.noContent().build();
    }

    @ApiOperation(value = "아이템 즐겨찾기 토글")
    @PatchMapping("/{itemId}/pin")
    public ResponseEntity<Void> toggleIsPinned(
            @PathVariable Integer storeId,
            @PathVariable Integer itemId,
            @LoginUser LoginContext login) {
        itemService.toggleItemPin(login, storeId, itemId);
        return ResponseEntity.noContent().build();
    }
}
