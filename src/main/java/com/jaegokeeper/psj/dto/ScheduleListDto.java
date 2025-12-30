package com.jaegokeeper.psj.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleListDto {
    private Integer albaId;

    private Integer workId;

    private Integer scheduleId;

    private LocalDateTime workIn;

    private LocalDateTime workOut;
}
