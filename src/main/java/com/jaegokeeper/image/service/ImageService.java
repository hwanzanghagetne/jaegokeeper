package com.jaegokeeper.image.service;

import com.jaegokeeper.common.DateTimeConstants;
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

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;

import static com.jaegokeeper.exception.ErrorCode.*;

@Service
@RequiredArgsConstructor
public class ImageService {

    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "webp");
    private static final int MAX_ORIGIN_NAME_LENGTH = 255;
    private static final Duration PRESIGN_DURATION = Duration.ofMinutes(10);
    private static final byte[] JPEG_MAGIC = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_MAGIC = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A};
    private static final byte[] RIFF_MAGIC = "RIFF".getBytes(StandardCharsets.US_ASCII);
    private static final byte[] WEBP_MAGIC = "WEBP".getBytes(StandardCharsets.US_ASCII);

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

    public ImageInfoDTO uploadIfPresent(MultipartFile file) {
        if (file == null || file.isEmpty()) return null;
        return uploadImg(file);
    }

    public ImageInfoDTO uploadForUpdate(MultipartFile file, Boolean removeImage) {
        boolean hasFile = file != null && !file.isEmpty();
        if (hasFile && Boolean.TRUE.equals(removeImage)) {
            throw new BusinessException(IMAGE_UPDATE_CONFLICT);
        }
        return uploadIfPresent(file);
    }

    @Transactional
    public ImageInfoDTO uploadImg(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(BAD_REQUEST);
        }

        String originalName = sanitizeFileName(file.getOriginalFilename());
        String ext = getLowerExt(originalName);
        if (!ALLOWED_EXT.contains(ext)) {
            throw new BusinessException(IMAGE_INVALID_FORMAT);
        }

        String key = null;

        try {
            String mimeType = detectImageType(file);

            LocalDate d = LocalDate.now(DateTimeConstants.KST);
            String relDir = String.format("%04d/%02d/%02d", d.getYear(), d.getMonthValue(), d.getDayOfMonth());
            String storedName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
            key = relDir + "/" + storedName;

            try (InputStream in = file.getInputStream()) {
                s3Client.putObject(
                        PutObjectRequest.builder()
                                .bucket(bucketName)
                                .key(key)
                                .contentType(mimeType)
                                .build(),
                        RequestBody.fromInputStream(in, file.getSize())
                );
            }

            ImageInfoDTO dto = new ImageInfoDTO();
            dto.setOriginName(originalName.length() > MAX_ORIGIN_NAME_LENGTH
                    ? originalName.substring(0, MAX_ORIGIN_NAME_LENGTH) : originalName);
            dto.setImagePath(key);

            imageMapper.insertImgInfo(dto);

            return dto;
        } catch (BusinessException e) {
            deleteFromS3Quietly(key);
            throw e;
        } catch (Exception e) {
            deleteFromS3Quietly(key);
            throw new BusinessException(INTERNAL_ERROR, e);
        }
    }

    @Transactional(readOnly = true)
    public ImageInfoDTO findImgById(int storeId, int imageId) {
        ImageInfoDTO dto = imageMapper.findImgById(storeId, imageId);
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

    private String detectImageType(MultipartFile file) throws IOException {
        byte[] head = new byte[12];
        try (InputStream in = file.getInputStream()) {
            in.readNBytes(head, 0, head.length);
        }
        if (startsWith(head, JPEG_MAGIC, 0)) return "image/jpeg";
        if (startsWith(head, PNG_MAGIC, 0)) return "image/png";
        if (startsWith(head, RIFF_MAGIC, 0) && startsWith(head, WEBP_MAGIC, 8)) return "image/webp";
        throw new BusinessException(IMAGE_INVALID_FORMAT);
    }

    private boolean startsWith(byte[] data, byte[] prefix, int offset) {
        return Arrays.equals(Arrays.copyOfRange(data, offset, offset + prefix.length), prefix);
    }

    private void deleteFromS3Quietly(String key) {
        if (key == null || key.isBlank()) return;
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder().bucket(bucketName).key(key).build());
        } catch (Exception ignored) {
        }
    }
}
