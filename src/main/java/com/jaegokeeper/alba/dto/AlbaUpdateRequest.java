package com.jaegokeeper.alba.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AlbaUpdateRequest {

    private Integer albaId;
    private String albaName;
    private String albaPhone;
    private String albaStatus;
    private String albaEmail;
}
