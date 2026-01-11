package com.jaegokeeper.ddan.img.mapper;

import com.jaegokeeper.ddan.img.dto.ImgInfoDTO;

public interface ImgMapper {

    void insertImgInfo(ImgInfoDTO dto);

    ImgInfoDTO findImgById(int imageId);

}
