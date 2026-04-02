package com.jaegokeeper.image.service;

import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.image.dto.ImageInfoDTO;
import com.jaegokeeper.image.mapper.ImageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static com.jaegokeeper.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class ImageService {

    private static final String BASE_DIR = "/data/upload/img";
    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "webp");

    private final ImageMapper imageMapper;

    private String sanitizeFileName(String name) {
        if (name == null) return "unknown";

        name = name.replace("\\", "/");
        int slash = name.lastIndexOf('/');
        if (slash >= 0) name = name.substring(slash + 1);
        return name.trim();
    }

    private String getLowerExt(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) return "";
        return filename.substring(dot + 1).toLowerCase();
    }

    @Transactional
    public int uploadImg(ImageInfoDTO dto) throws IOException {
        MultipartFile file = dto.getFile();

        if (file == null || file.isEmpty()) {
            throw new BusinessException(BAD_REQUEST);
        }

        String originalName = sanitizeFileName(file.getOriginalFilename());
        String ext = getLowerExt(originalName);
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BusinessException(BAD_REQUEST);
        }

        LocalDate d = LocalDate.now();
        String relDir = String.format("/%04d/%02d/%02d", d.getYear(), d.getMonthValue(), d.getDayOfMonth());
        Path dirPath = Paths.get(BASE_DIR + relDir);
        Files.createDirectories(dirPath);

        String storedName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        String relPath = relDir + "/" + storedName;
        Path savePath = Paths.get(BASE_DIR + relPath);

        try {
            file.transferTo(savePath);
        } catch (Exception e) {
            throw new IOException("파일 저장 실패: " + e.getMessage(), e);
        }

        String mimeType = Files.probeContentType(savePath);
        if (mimeType == null || !mimeType.startsWith("image/")) {
            Files.deleteIfExists(savePath);
            throw new BusinessException(BAD_REQUEST);
        }

        dto.setOriginName(originalName);
        dto.setImagePath(String.valueOf(savePath));

        imageMapper.insertImgInfo(dto);

        return dto.getImageId();
    }

    @Transactional(readOnly = true)
    public ImageInfoDTO findImgById(int imageId) {
        ImageInfoDTO dto = imageMapper.findImgById(imageId);
        if (dto == null) {
            throw new BusinessException(IMAGE_NOT_FOUND);
        }
        return dto;
    }
}
