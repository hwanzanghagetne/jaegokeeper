package com.jaegokeeper.image.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@NoArgsConstructor
public class ImageInfoDTO {

    private int imageId;
    private String imagePath;
    private String originName;

    @JsonIgnore
    private MultipartFile file;

}
