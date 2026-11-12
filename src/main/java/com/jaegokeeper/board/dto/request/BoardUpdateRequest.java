package com.jaegokeeper.board.dto.request;

import com.jaegokeeper.board.enums.BoardWriterType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
public class BoardUpdateRequest {

    private String title;

    private String content;

    private BoardWriterType writerType;

    private Integer writerId;

    private Boolean removeImage;

    private MultipartFile file;
}
