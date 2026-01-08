package com.jaegokeeper.ddan.img.controller;

import com.jaegokeeper.ddan.img.dto.ImgInfoDTO;
import com.jaegokeeper.ddan.img.service.ImgService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;

@Controller
@RequestMapping("/img")
public class ImgController {

    private final ImgService imgService;
    public ImgController(ImgService imgService) {
        this.imgService = imgService;
    }

    /** 이미지 기초 UI */
    @RequestMapping("/main.jacho")
    public String imgMain(Model model) {

        model.addAttribute("view", "메인페이징");
        return "img/main";
    }

    /** 이미지 upload */
    @RequestMapping("/upload.jacho")
    public String imgUpload(ImgInfoDTO dto, RedirectAttributes redirect) {
        try {
            int imageId = imgService.uploadImg(dto);

            redirect.addFlashAttribute("success", true);
            redirect.addFlashAttribute("imageId", imageId);
            return "redirect:/img/main.jacho";
        } catch (IllegalArgumentException e) {
            redirect.addFlashAttribute("success", false);
            redirect.addFlashAttribute("message", e.getMessage());
            return "redirect:/img/main.jacho";
        } catch (IOException e) {
            redirect.addFlashAttribute("success", false);
            redirect.addFlashAttribute("message", "파일 업로드 중 오류가 발생했습니다.");
            return "redirect:/img/main.jacho";

        }
    }

    /** 이미지 get */
    @RequestMapping("/find/{imageId}")
    public ResponseEntity<?> findImgById(@PathVariable int imageId) {
        try {
            ImgInfoDTO info = imgService.findImgById(imageId);

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
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(500).body("이미지 로딩 중 오류가 발생했습니다.");
        }
    }

}
