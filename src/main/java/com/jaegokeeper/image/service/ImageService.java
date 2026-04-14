package com.jaegokeeper.image.service;

import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.image.dto.ImageInfoDTO;
import com.jaegokeeper.image.mapper.ImageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.*;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

import static com.jaegokeeper.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class ImageService {

    private static final String ENV_IMAGE_BASE_DIR = "IMAGE_BASE_DIR";
    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "webp");

    private final ImageMapper imageMapper;

    private Path getBaseDirPath() {
        String configured = System.getenv(ENV_IMAGE_BASE_DIR);
        if (configured != null && !configured.isBlank()) {
            return Paths.get(configured);
        }
        return Paths.get(System.getProperty("user.home"), "jaegokeeper", "upload", "img");
    }

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
    public int uploadImg(ImageInfoDTO dto) {
        MultipartFile file = dto.getFile();
        Path savedPath = null;

        if (file == null || file.isEmpty()) {
            throw new BusinessException(BAD_REQUEST);
        }

        String originalName = sanitizeFileName(file.getOriginalFilename());
        String ext = getLowerExt(originalName);
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BusinessException(IMAGE_INVALID_FORMAT);
        }

        try {
            Path baseDirPath = getBaseDirPath().toAbsolutePath().normalize();
            LocalDate d = LocalDate.now();
            String relDir = String.format("%04d/%02d/%02d", d.getYear(), d.getMonthValue(), d.getDayOfMonth());
            Path dirPath = baseDirPath.resolve(relDir).normalize();
            if (!dirPath.startsWith(baseDirPath)) {
                throw new BusinessException(BAD_REQUEST);
            }
            Files.createDirectories(dirPath);

            String storedName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            String relPath = relDir + "/" + storedName;
            savedPath = dirPath.resolve(storedName).normalize();
            if (!savedPath.startsWith(baseDirPath)) {
                throw new BusinessException(BAD_REQUEST);
            }

            file.transferTo(savedPath);

            String mimeType = Files.probeContentType(savedPath);
            if (mimeType == null || !mimeType.startsWith("image/")) {
                Files.deleteIfExists(savedPath);
                throw new BusinessException(IMAGE_INVALID_FORMAT);
            }

            dto.setOriginName(originalName);
            // DB에는 상대 경로만 저장하고, 실제 절대 경로는 조회 시 base dir로 resolve한다.
            dto.setImagePath(relPath);

            imageMapper.insertImgInfo(dto);

            return dto.getImageId();
        } catch (BusinessException e) {
            deleteQuietly(savedPath);
            throw e;
        } catch (Exception e) {
            deleteQuietly(savedPath);
            throw new BusinessException(INTERNAL_ERROR);
        }
    }

    @Transactional(readOnly = true)
    public ImageInfoDTO findImgById(int imageId) {
        ImageInfoDTO dto = imageMapper.findImgById(imageId);
        if (dto == null) {
            throw new BusinessException(IMAGE_NOT_FOUND);
        }
        return dto;
    }

    @Transactional(readOnly = true)
    public Path resolveImagePath(String storedPath) {
        if (storedPath == null || storedPath.isBlank()) {
            throw new BusinessException(IMAGE_NOT_FOUND);
        }

        Path rawPath = Paths.get(storedPath);
        if (rawPath.isAbsolute()) {
            // 하위 호환: 과거 절대경로 저장 데이터도 그대로 조회 가능하게 유지
            return rawPath.normalize();
        }

        Path baseDirPath = getBaseDirPath().toAbsolutePath().normalize();
        Path resolved = baseDirPath.resolve(storedPath).normalize();
        if (!resolved.startsWith(baseDirPath)) {
            throw new BusinessException(BAD_REQUEST);
        }
        return resolved;
    }

    // 파일만 삭제 — DB 레코드는 트랜잭션 롤백이 처리하므로 건드리지 않는다.
    public void deleteImageFile(String relPath) {
        if (relPath == null || relPath.isBlank()) return;
        try {
            deleteQuietly(resolveImagePath(relPath));
        } catch (Exception ignored) {
            // 경로 resolve 실패 시에도 원인 예외를 덮지 않는다.
        }
    }

    private void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (Exception ignored) {
            // cleanup 실패는 원인 예외를 덮지 않는다.
        }
    }
}
