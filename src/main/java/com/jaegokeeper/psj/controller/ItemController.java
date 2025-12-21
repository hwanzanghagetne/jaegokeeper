package com.jaegokeeper.psj.controller;

import com.jaegokeeper.psj.dto.ItemDto;
import com.jaegokeeper.psj.service.ItemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @PostMapping("/sj")
    public ResponseEntity<String> mapRequest(@Valid @RequestBody ItemDto item) {
        itemService.saveDto(item);
        return ResponseEntity.ok("저장 완료");
    }
    // GET - 전체 조회
    @GetMapping("/sj")
    public ResponseEntity<List<ItemDto>> getAllItems() {
        List<ItemDto> items = itemService.getAllItems();
        return ResponseEntity.ok(items);
    }
}
