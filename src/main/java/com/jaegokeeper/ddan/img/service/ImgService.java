package com.jaegokeeper.ddan.img.service;

import com.jaegokeeper.ddan.img.dto.ImgInfoDTO;

import java.io.IOException;

public interface ImgService {

    int uploadImg(ImgInfoDTO dto) throws IOException;

    ImgInfoDTO findImgById(int imageId);

}
