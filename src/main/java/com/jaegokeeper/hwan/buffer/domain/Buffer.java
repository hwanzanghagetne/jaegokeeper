package com.jaegokeeper.hwan.buffer.domain;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class Buffer {

    private Integer bufferId;
    private Integer itemId;
    private Integer bufferAmount;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
