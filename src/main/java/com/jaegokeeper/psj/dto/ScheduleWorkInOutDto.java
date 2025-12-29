package com.jaegokeeper.psj.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleWorkInOutDto {
    private Integer albaId;

    private LocalDateTime workIn;

    private LocalDateTime workOut;

    private LocalDate workDate;
}
