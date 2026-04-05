package com.jaegokeeper.schedule.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
public class ScheduleRegisterRequest {

    private Integer albaId;

    @NotBlank(message = "알바 근무 요일은 필수입니다.")
    @Size(max = 50, message = "알바 근무 요일은 최대 50자까지 입력 가능합니다.")
    private String scheduleTime;
}
