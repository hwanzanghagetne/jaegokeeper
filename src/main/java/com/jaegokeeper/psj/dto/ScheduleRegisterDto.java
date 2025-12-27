package com.jaegokeeper.psj.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.sql.Time;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleRegisterDto {
    private Integer albaId;

    private Integer scheduleId;

    @NotBlank(message = "알바 근무 요일은 필수입니다.")
    @Size(max = 50, message = "알바 근무 요일은 최대 50자까지 입력 가능합니다.")
    private String scheduleTime;

}
