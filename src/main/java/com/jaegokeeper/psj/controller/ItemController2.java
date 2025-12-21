package com.jaegokeeper.psj.controller;

import com.jaegokeeper.psj.dto.ItemDto2;
import com.jaegokeeper.psj.service.ItemService2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ItemController2 {

    private final ItemService2 itemService;

    public ItemController2(ItemService2 itemService) {
        this.itemService = itemService;
    }

    @PostMapping("/sj")
    public ResponseEntity<String> mapRequest(@Valid @RequestBody ItemDto2 item) {
        itemService.saveDto(item);
        return ResponseEntity.ok("저장 완료");
    }
    // GET - 전체 조회
    @GetMapping("/sj")
    public ResponseEntity<List<ItemDto2>> getAllItems() {
        List<ItemDto2> items = itemService.getAllItems();
        return ResponseEntity.ok(items);
    }
}
