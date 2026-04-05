package com.jaegokeeper.image.controller;

import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.image.dto.ImageInfoDTO;
import com.jaegokeeper.image.dto.ImageUploadRequest;
import com.jaegokeeper.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.*;

import static com.jaegokeeper.exception.ErrorCode.INTERNAL_ERROR;

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
    public ResponseEntity<Resource> findImgById(@PathVariable int imageId) {
        ImageInfoDTO info = imageService.findImgById(imageId);

        try {
            Path path = Paths.get(info.getImagePath());
            if (!Files.exists(path)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new UrlResource(path.toUri());
            String contentType = Files.probeContentType(path);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);
        } catch (Exception e) {
            throw new BusinessException(INTERNAL_ERROR);
        }
    }
}
