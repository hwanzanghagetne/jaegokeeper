package com.jaegokeeper.schedule.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
public class ScheduleUpdateRequest {

    private Integer scheduleId;

    @Size(max = 20, message = "스케줄 시간은 최대 20자까지 입력 가능합니다")
    private String scheduleTime;
}
