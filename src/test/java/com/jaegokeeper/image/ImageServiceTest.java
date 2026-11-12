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
import static org.junit.Assert.assertNull;
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

    private MultipartFile fileWith(String filename, byte[] content) {
        return new MockMultipartFile("file", filename, "image/png", content);
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
        BusinessException e = assertThrows(BusinessException.class,
                () -> imageService.uploadImg(null));

        assertEquals(ErrorCode.BAD_REQUEST, e.getErrorCode());
    }

    @Test
    public void 업로드_파일이비어있음_예외() {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        BusinessException e = assertThrows(BusinessException.class,
                () -> imageService.uploadImg(file));

        assertEquals(ErrorCode.BAD_REQUEST, e.getErrorCode());
    }

    @Test
    public void 업로드_허용되지않는확장자_예외() {
        BusinessException e = assertThrows(BusinessException.class,
                () -> imageService.uploadImg(fileWith("document.txt", PNG)));

        assertEquals(ErrorCode.IMAGE_INVALID_FORMAT, e.getErrorCode());
    }

    @Test
    public void 업로드_확장자없는파일명_예외() {
        BusinessException e = assertThrows(BusinessException.class,
                () -> imageService.uploadImg(fileWith("noextension", PNG)));

        assertEquals(ErrorCode.IMAGE_INVALID_FORMAT, e.getErrorCode());
    }

    @Test
    public void 업로드_확장자만_이미지인_위장파일은_거부하고_S3에_올리지_않음() {
        byte[] html = "<html><script>alert(1)</script></html>".getBytes(StandardCharsets.UTF_8);

        BusinessException e = assertThrows(BusinessException.class,
                () -> imageService.uploadImg(fileWith("evil.png", html)));

        assertEquals(ErrorCode.IMAGE_INVALID_FORMAT, e.getErrorCode());
        verifyNoInteractions(s3Client);
        verifyNoInteractions(imageMapper);
    }

    @Test
    public void 업로드_JPEG는_콘텐츠타입_image_jpeg로_저장() {
        stubInsertWithId(7);

        ImageInfoDTO uploaded = imageService.uploadImg(fileWith("photo.jpg", JPEG));

        assertEquals(7, uploaded.getImageId());
        assertEquals("photo.jpg", uploaded.getOriginName());
        assertTrue(uploaded.getImagePath().endsWith(".jpg"));
        assertEquals("image/jpeg", capturedPutRequest().contentType());
    }

    @Test
    public void 업로드_원본_파일명이_255자를_넘으면_잘라서_저장() {
        stubInsertWithId(1);

        ImageInfoDTO uploaded = imageService.uploadImg(fileWith("a".repeat(300) + ".png", PNG));

        assertEquals(255, uploaded.getOriginName().length());
    }

    @Test
    public void 업로드_PNG와_WebP도_파일내용으로_판별() {
        stubInsertWithId(1);

        imageService.uploadImg(fileWith("a.png", PNG));
        imageService.uploadImg(fileWith("b.webp", WEBP));

        ArgumentCaptor<PutObjectRequest> captor = ArgumentCaptor.forClass(PutObjectRequest.class);
        verify(s3Client, times(2)).putObject(captor.capture(), any(RequestBody.class));
        assertEquals("image/png", captor.getAllValues().get(0).contentType());
        assertEquals("image/webp", captor.getAllValues().get(1).contentType());
    }

    @Test
    public void 업로드_확장자와_내용이_달라도_내용을_기준으로_콘텐츠타입_저장() {
        stubInsertWithId(1);

        imageService.uploadImg(fileWith("photo.jpg", PNG));

        assertEquals("image/png", capturedPutRequest().contentType());
    }

    @Test
    public void 업로드_DB저장_실패시_올린_S3_객체_삭제() {
        doThrow(new IllegalStateException("db down")).when(imageMapper).insertImgInfo(any(ImageInfoDTO.class));

        BusinessException e = assertThrows(BusinessException.class,
                () -> imageService.uploadImg(fileWith("photo.jpg", JPEG)));

        assertEquals(ErrorCode.INTERNAL_ERROR, e.getErrorCode());
        ArgumentCaptor<DeleteObjectRequest> captor = ArgumentCaptor.forClass(DeleteObjectRequest.class);
        verify(s3Client).deleteObject(captor.capture());
        assertEquals("test-bucket", captor.getValue().bucket());
        assertTrue(captor.getValue().key().endsWith(".jpg"));
    }

    @Test
    public void 파일이_있으면_업로드하고_없으면_null() {
        stubInsertWithId(3);

        assertEquals(3, imageService.uploadIfPresent(fileWith("a.png", PNG)).getImageId());
        assertNull(imageService.uploadIfPresent(null));
        assertNull(imageService.uploadIfPresent(new MockMultipartFile("file", "a.png", "image/png", new byte[0])));
    }

    @Test
    public void 수정_파일_업로드와_이미지_삭제를_동시에_요청하면_예외() {
        BusinessException e = assertThrows(BusinessException.class,
                () -> imageService.uploadForUpdate(fileWith("a.png", PNG), true));

        assertEquals(ErrorCode.IMAGE_UPDATE_CONFLICT, e.getErrorCode());
        verifyNoInteractions(s3Client);
    }

    @Test
    public void 수정_삭제만_요청하거나_아무것도_없으면_업로드하지_않음() {
        assertNull(imageService.uploadForUpdate(null, true));
        assertNull(imageService.uploadForUpdate(null, null));
        assertNull(imageService.uploadForUpdate(new MockMultipartFile("file", "a.png", "image/png", new byte[0]), true));

        verifyNoInteractions(s3Client);
    }

    @Test
    public void 수정_파일만_있으면_업로드() {
        stubInsertWithId(8);

        assertEquals(8, imageService.uploadForUpdate(fileWith("a.png", PNG), null).getImageId());
        assertEquals(8, imageService.uploadForUpdate(fileWith("a.png", PNG), false).getImageId());
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
