package com.jaegokeeper.schedule.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ScheduleUpdateRequest {

    private Integer scheduleId;
    private String scheduleTime;
}
