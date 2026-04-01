package com.jaegokeeper.image.mapper;

import com.jaegokeeper.image.dto.ImageInfoDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ImageMapper {

    void insertImgInfo(ImageInfoDTO dto);

    ImageInfoDTO findImgById(int imageId);

}
