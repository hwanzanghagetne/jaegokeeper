package com.jaegokeeper.image.controller;

import com.jaegokeeper.image.dto.ImageInfoDTO;
import com.jaegokeeper.image.dto.ImageUploadRequest;
import com.jaegokeeper.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/img")
public class ImageController {

    private final ImageService imageService;

    @PostMapping("/upload")
    public ResponseEntity<Integer> uploadImg(ImageUploadRequest req) {
        ImageInfoDTO dto = new ImageInfoDTO();
        dto.setFile(req.getFile());
        int imageId = imageService.uploadImg(dto);
        return ResponseEntity.status(201).body(imageId);
    }

    @GetMapping("/find/{imageId}")
    public ResponseEntity<Void> findImgById(@PathVariable int imageId) {
        ImageInfoDTO info = imageService.findImgById(imageId);
        String presignedUrl = imageService.generatePresignedUrl(info.getImagePath());

        return ResponseEntity.status(302)
                .header(HttpHeaders.LOCATION, presignedUrl)
                .build();
    }
}
