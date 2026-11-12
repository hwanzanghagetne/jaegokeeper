package com.jaegokeeper.image.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import com.jaegokeeper.auth.dto.LoginContext;
import com.jaegokeeper.image.dto.ImageInfoDTO;
import com.jaegokeeper.image.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Image")
@RestController
@RequiredArgsConstructor
@RequestMapping("/img")
public class ImageController {

    private final ImageService imageService;

    @GetMapping("/find/{imageId}")
    public ResponseEntity<Void> findImgById(
            @PathVariable int imageId,
            @AuthenticationPrincipal LoginContext login) {
        ImageInfoDTO info = imageService.findImgById(login.getStoreId(), imageId);
        String presignedUrl = imageService.generatePresignedUrl(info.getImagePath());
        return ResponseEntity.status(302)
                .header(HttpHeaders.LOCATION, presignedUrl)
                .build();
    }
}
