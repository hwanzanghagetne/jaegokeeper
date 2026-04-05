package com.jaegokeeper.schedule.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ScheduleDetailResponse {

    private Integer scheduleId;
    private Integer albaId;
    private String scheduleTime;
}
