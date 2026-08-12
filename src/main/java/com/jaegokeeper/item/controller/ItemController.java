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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Tag(name = "Item")
@RestController
@RequestMapping("/stores/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;

    @Operation(summary = "아이템 생성")
    @PostMapping
    public ResponseEntity<ItemCreateResponse> createItem(
            @Valid @ModelAttribute ItemCreateRequest dto,
            @LoginUser LoginContext login) {
        Integer itemId = itemService.createItem(login, dto);
        return ResponseEntity.status(201).body(new ItemCreateResponse(itemId));
    }

    @Operation(summary = "아이템 목록 조회")
    @GetMapping
    public ResponseEntity<PageResponse<ItemListResponse>> getItems(
            @Valid @ModelAttribute ItemPageRequest dto,
            @LoginUser LoginContext login) {
        return ResponseEntity.ok(itemService.getItemList(login, dto));
    }

    @Operation(summary = "아이템 상세 조회")
    @GetMapping("/{itemId}")
    public ResponseEntity<ItemDetailResponse> getItemDetail(
            @PathVariable Integer itemId,
            @LoginUser LoginContext login) {
        return ResponseEntity.ok(itemService.getItemDetail(login, itemId));
    }

    @Operation(summary = "아이템 수정")
    @PutMapping("/{itemId}")
    public ResponseEntity<Void> modifyItem(
            @PathVariable Integer itemId,
            @Valid @ModelAttribute ItemUpdateRequest dto,
            @LoginUser LoginContext login) {
        itemService.updateItem(login, itemId, dto);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "아이템 삭제")
    @DeleteMapping("/{itemId}")
    public ResponseEntity<Void> deleteItem(
            @PathVariable Integer itemId,
            @LoginUser LoginContext login) {
        itemService.softDeleteItem(login, itemId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "아이템 즐겨찾기 토글")
    @PatchMapping("/{itemId}/pin")
    public ResponseEntity<Void> toggleIsPinned(
            @PathVariable Integer itemId,
            @LoginUser LoginContext login) {
        itemService.toggleItemPin(login, itemId);
        return ResponseEntity.noContent().build();
    }
}
