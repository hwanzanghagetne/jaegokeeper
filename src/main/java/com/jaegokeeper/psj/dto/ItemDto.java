package com.jaegokeeper.psj.dto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private int itemId;

    @NotNull(message = "스토어 아이디는 null입니다.")
    private Integer storeId;

    @NotBlank(message = "아이템 이름은 필수입니다.")
    @Size(max = 100, message = "아이템 이름은 최대 100자까지 입력 가능합니다.")
    private String itemName;
}