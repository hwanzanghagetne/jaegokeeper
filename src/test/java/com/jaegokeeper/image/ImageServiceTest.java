package com.jaegokeeper.image;

import com.jaegokeeper.exception.BusinessException;
import com.jaegokeeper.exception.ErrorCode;
import com.jaegokeeper.image.dto.ImageInfoDTO;
import com.jaegokeeper.image.mapper.ImageMapper;
import com.jaegokeeper.image.service.ImageService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class ImageServiceTest {

    private static final byte[] JPEG = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 0, 0, 0, 0, 0, 0, 0};
    private static final byte[] PNG = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0, 0, 0};
    private static final byte[] WEBP = {'R', 'I', 'F', 'F', 0, 0, 0, 0, 'W', 'E', 'B', 'P'};

    @InjectMocks
    private ImageService imageService;

    @Mock
    private ImageMapper imageMapper;

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(imageService, "bucketName", "test-bucket");
    }

    private ImageInfoDTO dtoWith(String filename, byte[] content) {
        ImageInfoDTO dto = new ImageInfoDTO();
        dto.setFile(new MockMultipartFile("file", filename, "image/png", content));
        return dto;
    }

    private void stubInsertWithId(int imageId) {
        doAnswer(invocation -> {
            ImageInfoDTO arg = invocation.getArgument(0);
            arg.setImageId(imageId);
            return null;
        }).when(imageMapper).insertImgInfo(any(ImageInfoDTO.class));
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
        BusinessException e = assertThrows(BusinessException.class,
                () -> imageService.uploadImg(dtoWith("document.txt", PNG)));

        assertEquals(ErrorCode.IMAGE_INVALID_FORMAT, e.getErrorCode());
    }

    @Test
    public void 업로드_확장자없는파일명_예외() {
        BusinessException e = assertThrows(BusinessException.class,
                () -> imageService.uploadImg(dtoWith("noextension", PNG)));

        assertEquals(ErrorCode.IMAGE_INVALID_FORMAT, e.getErrorCode());
    }

    @Test
    public void 업로드_확장자만_이미지인_위장파일은_거부하고_S3에_올리지_않음() {
        byte[] html = "<html><script>alert(1)</script></html>".getBytes(StandardCharsets.UTF_8);

        BusinessException e = assertThrows(BusinessException.class,
                () -> imageService.uploadImg(dtoWith("evil.png", html)));

        assertEquals(ErrorCode.IMAGE_INVALID_FORMAT, e.getErrorCode());
        verifyNoInteractions(s3Client);
        verifyNoInteractions(imageMapper);
    }

    @Test
    public void 업로드_JPEG는_콘텐츠타입_image_jpeg로_저장() {
        stubInsertWithId(7);

        int imageId = imageService.uploadImg(dtoWith("photo.jpg", JPEG));

        assertEquals(7, imageId);
        assertEquals("image/jpeg", capturedPutRequest().contentType());
    }

    @Test
    public void 업로드_PNG와_WebP도_파일내용으로_판별() {
        stubInsertWithId(1);

        imageService.uploadImg(dtoWith("a.png", PNG));
        imageService.uploadImg(dtoWith("b.webp", WEBP));

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client, times(2)).putObject(captor.capture(), any(RequestBody.class));
        assertEquals("image/png", captor.getAllValues().get(0).contentType());
        assertEquals("image/webp", captor.getAllValues().get(1).contentType());
    }

    @Test
    public void 업로드_확장자와_내용이_달라도_내용을_기준으로_콘텐츠타입_저장() {
        stubInsertWithId(1);

        imageService.uploadImg(dtoWith("photo.jpg", PNG));

        assertEquals("image/png", capturedPutRequest().contentType());
    }

    @Test
    public void 업로드_DB저장_실패시_올린_S3_객체_삭제() {
        doThrow(new IllegalStateException("db down")).when(imageMapper).insertImgInfo(any(ImageInfoDTO.class));

        BusinessException e = assertThrows(BusinessException.class,
                () -> imageService.uploadImg(dtoWith("photo.jpg", JPEG)));

        assertEquals(ErrorCode.INTERNAL_ERROR, e.getErrorCode());
        ArgumentCaptor<DeleteObjectRequest> captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(captor.capture());
        assertEquals("test-bucket", captor.getValue().bucket());
        assertTrue(captor.getValue().key().endsWith(".jpg"));
    }

    @Test
    public void 이미지조회_내매장_이미지면_반환() {
        ImageInfoDTO found = new ImageInfoDTO();
        found.setImagePath("2026/11/10/a.png");
        when(imageMapper.findImgById(1, 5)).thenReturn(found);

        assertEquals(found, imageService.findImgById(1, 5));
    }

    @Test
    public void 이미지조회_없거나_다른매장_이미지면_예외() {
        when(imageMapper.findImgById(1, 999)).thenReturn(null);

        BusinessException e = assertThrows(BusinessException.class,
                () -> imageService.findImgById(1, 999));

        assertEquals(ErrorCode.IMAGE_NOT_FOUND, e.getErrorCode());
    }

    @Test
    public void presignedUrl_빈키_예외() {
        BusinessException e = assertThrows(BusinessException.class,
                () -> imageService.generatePresignedUrl(""));

        assertEquals(ErrorCode.IMAGE_NOT_FOUND, e.getErrorCode());
    }

    @Test
    public void presignedUrl_널키_예외() {
        BusinessException e = assertThrows(BusinessException.class,
                () -> imageService.generatePresignedUrl(null));

        assertEquals(ErrorCode.IMAGE_NOT_FOUND, e.getErrorCode());
    }

    @Test
    public void 파일삭제_빈경로는_S3를_호출하지_않고_무시() {
        imageService.deleteImageFile(null);
        imageService.deleteImageFile("");

        verify(s3Client, never()).deleteObject(any(DeleteObjectRequest.class));
    }

    private PutObjectRequest capturedPutRequest() {
        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client).putObject(captor.capture(), any(RequestBody.class));
        return captor.getValue();
    }
}
