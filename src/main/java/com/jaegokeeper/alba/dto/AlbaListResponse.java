package com.jaegokeeper.alba.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AlbaListResponse {

    private Integer albaId;
    private Integer imageId;
    private Integer workId;
    private String workStatus;
    private String workDate;
    private String scheduleTimes;
    private String albaName;
    private String albaPhone;
    private String albaEmail;
    private String albaStatus;
}
