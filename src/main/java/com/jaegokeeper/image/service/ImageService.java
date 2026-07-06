package com.jaegokeeper.image.service;

import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.image.dto.ImageInfoDTO;
import com.jaegokeeper.image.mapper.ImageMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Set;
import java.util.UUID;

import static com.jaegokeeper.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class ImageService {

    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "webp");
    private static final Duration PRESIGN_DURATION = Duration.ofMinutes(10);
    // 서버(EC2) OS 타임존이 UTC라 LocalDate.now()가 서버 로컬시간과 어긋날 수 있어 명시적으로 지정
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final ImageMapper imageMapper;
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${S3_BUCKET_NAME}")
    private String bucketName;

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

        if (file == null || file.isEmpty()) {
            throw new BusinessException(BAD_REQUEST);
        }

        String originalName = sanitizeFileName(file.getOriginalFilename());
        String ext = getLowerExt(originalName);
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BusinessException(IMAGE_INVALID_FORMAT);
        }

        Path tempPath = null;
        String key = null;

        try {
            // 확장자만으로는 위조 가능하니, 임시 파일로 받아 실제 콘텐츠 타입을 확인한 뒤 S3에 올린다.
            tempPath = Files.createTempFile("img-upload-", "." + ext);
            file.transferTo(tempPath);

            String mimeType = Files.probeContentType(tempPath);
            if (mimeType == null || !mimeType.startsWith("image/")) {
                throw new BusinessException(IMAGE_INVALID_FORMAT);
            }

            LocalDate d = LocalDate.now(KST);
            String relDir = String.format("%04d/%02d/%02d", d.getYear(), d.getMonthValue(), d.getDayOfMonth());
            String storedName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            key = relDir + "/" + storedName;

            s3Client.putObject(
                    PutObjectRequest.builder()
                            .bucket(bucketName)
                            .key(key)
                            .contentType(mimeType)
                            .build(),
                    RequestBody.fromFile(tempPath)
            );

            dto.setOriginName(originalName);
            // DB에는 S3 객체 키만 저장하고, 실제 접근 URL은 조회 시 presigned URL로 발급한다.
            dto.setImagePath(key);

            imageMapper.insertImgInfo(dto);

            return dto.getImageId();
        } catch (BusinessException e) {
            deleteFromS3Quietly(key);
            throw e;
        } catch (Exception e) {
            deleteFromS3Quietly(key);
            throw new BusinessException(INTERNAL_ERROR, e);
        } finally {
            deleteTempQuietly(tempPath);
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
    public String generatePresignedUrl(String key) {
        if (key == null || key.isBlank()) {
            throw new BusinessException(IMAGE_NOT_FOUND);
        }

        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(PRESIGN_DURATION)
                .getObjectRequest(getObjectRequest)
                .build();

        return s3Presigner.presignGetObject(presignRequest).url().toString();
    }

    // DB 레코드는 트랜잭션 롤백이 처리하므로 건드리지 않고, S3 객체만 삭제한다.
    public void deleteImageFile(String key) {
        deleteFromS3Quietly(key);
    }

    private void deleteFromS3Quietly(String key) {
        if (key == null || key.isBlank()) return;
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(key).build());
        } catch (Exception ignored) {
            // cleanup 실패는 원인 예외를 덮지 않는다.
        }
    }

    private void deleteTempQuietly(Path path) {
        if (path == null) return;
        try {
            Files.deleteIfExists(path);
        } catch (Exception ignored) {
            // cleanup 실패는 원인 예외를 덮지 않는다.
        }
    }
}
