package com.jaegokeeper.image.service;

import com.jaegokeeper.image.dto.ImageInfoDTO;

import java.io.IOException;

public interface ImageService {

    int uploadImg(ImageInfoDTO dto) throws IOException;

    ImageInfoDTO findImgById(int imageId);

}
