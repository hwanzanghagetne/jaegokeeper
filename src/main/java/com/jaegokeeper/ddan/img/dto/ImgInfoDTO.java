package com.jaegokeeper.ddan.img.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class ImgInfoDTO {

    private int imageId;
    private String imagePath;
    private String originName;
    private MultipartFile file;

}
