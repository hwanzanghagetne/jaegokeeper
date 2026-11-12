package com.jaegokeeper.alba.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@NoArgsConstructor
public class Alba {

    @Setter
    private Integer albaId;

    private Integer storeId;
    private String albaName;
    private String albaPhone;
    private String albaEmail;
    private String albaStatus;
    private Integer imageId;

    public static Alba create(Integer storeId, String albaName, String albaPhone,
                              String albaEmail, String albaStatus, Integer imageId) {
        Alba alba = new Alba();
        alba.storeId = storeId;
        alba.albaName = albaName;
        alba.albaPhone = albaPhone;
        alba.albaEmail = albaEmail;
        alba.albaStatus = albaStatus;
        alba.imageId = imageId;
        return alba;
    }
}
