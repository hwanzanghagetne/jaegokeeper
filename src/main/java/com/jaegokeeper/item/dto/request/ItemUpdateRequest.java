package com.jaegokeeper.item.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import static com.jaegokeeper.stock.StockConstants.MAX_AMOUNT;

@Getter
@Setter
@NoArgsConstructor
public class ItemUpdateRequest {

    @Size(min = 1, max = 50, message = "itemName은 1자 이상 50자 이하입니다.")
    private String itemName;
    private Boolean isPinned;
    private Boolean removeImage;
    private MultipartFile file;

    @Min(value = 0, message = "bufferAmount는 0 이상입니다.")
    @Max(value = MAX_AMOUNT, message = "bufferAmount는 1,000,000 이하입니다.")
    private Integer bufferAmount;

    @Min(value = 0, message = "targetAmount는 0 이상입니다.")
    @Max(value = MAX_AMOUNT, message = "targetAmount는 1,000,000 이하입니다.")
    private Integer targetAmount;
}
