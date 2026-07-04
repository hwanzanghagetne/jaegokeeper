package com.jaegokeeper.image;

import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.exception.ErrorCode;
import com.jaegokeeper.image.dto.ImageInfoDTO;
import com.jaegokeeper.image.mapper.ImageMapper;
import com.jaegokeeper.image.service.ImageService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ImageServiceTest {

    @InjectMocks
    private ImageService imageService;

    @Mock
    private ImageMapper imageMapper;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void 업로드_파일없음_예외() {
        ImageInfoDTO dto = new ImageInfoDTO();
        dto.setFile(null);

        BusinessException e = assertThrows(BusinessException.class,
                () -> imageService.uploadImg(dto));

        assertEquals(ErrorCode.BAD_REQUEST, e.getErrorCode());
    }

    @Test
    public void 업로드_파일이비어있음_예외() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        ImageInfoDTO dto = new ImageInfoDTO();
        dto.setFile(file);

        BusinessException e = assertThrows(BusinessException.class,
                () -> imageService.uploadImg(dto));

        assertEquals(ErrorCode.BAD_REQUEST, e.getErrorCode());
    }

    @Test
    public void 업로드_허용되지않는확장자_예외() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("document.txt");

        ImageInfoDTO dto = new ImageInfoDTO();
        dto.setFile(file);

        BusinessException e = assertThrows(BusinessException.class,
                () -> imageService.uploadImg(dto));

        assertEquals(ErrorCode.IMAGE_INVALID_FORMAT, e.getErrorCode());
    }

    @Test
    public void 업로드_확장자없는파일명_예외() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("noextension");

        ImageInfoDTO dto = new ImageInfoDTO();
        dto.setFile(file);

        BusinessException e = assertThrows(BusinessException.class,
                () -> imageService.uploadImg(dto));

        assertEquals(ErrorCode.IMAGE_INVALID_FORMAT, e.getErrorCode());
    }

    @Test
    public void 이미지조회_없음_예외() {
        when(imageMapper.findImgById(999)).thenReturn(null);

        BusinessException e = assertThrows(BusinessException.class,
                () -> imageService.findImgById(999));

        assertEquals(ErrorCode.IMAGE_NOT_FOUND, e.getErrorCode());
    }

    @Test
    public void 경로resolve_빈경로_예외() {
        BusinessException e = assertThrows(BusinessException.class,
                () -> imageService.resolveImagePath(""));

        assertEquals(ErrorCode.IMAGE_NOT_FOUND, e.getErrorCode());
    }

    @Test
    public void 경로resolve_널경로_예외() {
        BusinessException e = assertThrows(BusinessException.class,
                () -> imageService.resolveImagePath(null));

        assertEquals(ErrorCode.IMAGE_NOT_FOUND, e.getErrorCode());
    }

    @Test
    public void 경로resolve_상위경로탈출시도_예외() {
        BusinessException e = assertThrows(BusinessException.class,
                () -> imageService.resolveImagePath("../../etc/passwd"));

        assertEquals(ErrorCode.BAD_REQUEST, e.getErrorCode());
    }

    @Test
    public void 파일삭제_빈경로는_예외없이무시() {
        // relPath가 null/blank면 아무 것도 하지 않고 조용히 반환되어야 한다
        imageService.deleteImageFile(null);
        imageService.deleteImageFile("");
        // 예외가 발생하지 않으면 성공
    }
}
