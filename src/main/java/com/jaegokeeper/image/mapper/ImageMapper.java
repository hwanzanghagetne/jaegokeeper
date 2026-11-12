package com.jaegokeeper.image.mapper;

import com.jaegokeeper.image.dto.ImageInfoDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ImageMapper {

    void insertImgInfo(ImageInfoDTO dto);

    ImageInfoDTO findImgById(@Param("storeId") int storeId, @Param("imageId") int imageId);
}
