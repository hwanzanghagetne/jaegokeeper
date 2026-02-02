package com.jaegokeeper.ddan.img.mapper;

import com.jaegokeeper.ddan.img.dto.ImgInfoDTO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ImgMapper {

    void insertImgInfo(ImgInfoDTO dto);

    ImgInfoDTO findImgById(int imageId);

}
