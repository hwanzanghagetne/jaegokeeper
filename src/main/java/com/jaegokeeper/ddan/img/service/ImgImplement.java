package com.jaegokeeper.ddan.img.service;

import com.jaegokeeper.ddan.img.dto.ImgInfoDTO;
import com.jaegokeeper.ddan.img.mapper.ImgMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Service
public class ImgImplement implements ImgService{

    private static final String BASE_DIR = "/data/upload/img";
    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "webp");

    private final ImgMapper imgMapper;
    public ImgImplement(ImgMapper imgMapper) {
        this.imgMapper = imgMapper;
    }

    // 공백, 경로 등 맞추기
    private String sanitizeFileName(String name) {
        if (name == null) return "unknown";

        name = name.replace("\\", "/");
        int slash = name.lastIndexOf('/');
        if (slash >= 0) name = name.substring(slash + 1);
        return name.trim();
    }

    // 대소문자 맞추기
    private String getLowerExt(String filename) {
        int dot = filename.lastIndexOf('.');
        if (dot < 0 || dot == filename.length() - 1) return "";
        return filename.substring(dot + 1).toLowerCase();
    }

    @Override
    @Transactional
    public int uploadImg(ImgInfoDTO dto) throws IOException {
        MultipartFile file = dto.getFile();

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("파일이 비어있습니다.");
        }

        String originalName = sanitizeFileName(file.getOriginalFilename());
        String ext = getLowerExt(originalName);
        if (!ALLOWED_EXT.contains(ext)) {
            throw new IllegalArgumentException("허용되지 않은 확장자입니다: " + ext);
        }

        // 날짜 폴더
        LocalDate d = LocalDate.now();
        String relDir = String.format("/%04d/%02d/%02d", d.getYear(), d.getMonthValue(), d.getDayOfMonth());
        Path dirPath = Paths.get(BASE_DIR + relDir);
        Files.createDirectories(dirPath);

        // 저장 파일명
        String storedName = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        String relPath = relDir + "/" + storedName;          // DB 저장용
        Path savePath = Paths.get(BASE_DIR + relPath);  // 실제 저장

        // 저장
        try {
            file.transferTo(savePath);
        } catch (Exception e) {
            throw new IOException("파일 저장 실패: " + e.getMessage(), e);
        }

        // DB insert
        dto.setOriginName(originalName);
        dto.setImagePath(String.valueOf(savePath));

        imgMapper.insertImgInfo(dto);

        // useGeneratedKeys로 PK가 imageId에 세팅됨
        return dto.getImageId();
    }

    @Override
    @Transactional(readOnly = true)
    public ImgInfoDTO findImgById(int imageId) {
        ImgInfoDTO dto = imgMapper.findImgById(imageId);
        if (dto == null) {
            throw new IllegalArgumentException("해당 ID의 이미지가 없습니다. id=" + imageId);
        }
        return dto;
    }

}
