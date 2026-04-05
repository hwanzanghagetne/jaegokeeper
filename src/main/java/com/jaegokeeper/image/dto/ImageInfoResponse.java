package com.jaegokeeper.image.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ImageInfoResponse {

    private int imageId;
    private String imagePath;
    private String originName;
}
