package com.jaegokeeper.schedule.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class WorkInOutRequest {

    @NotNull(message = "albaId는 필수입니다")
    private Integer albaId;

    @Size(max = 20, message = "workStatus는 20자 이하입니다.")
    private String workStatus;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime workIn;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime workOut;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate workDate;
}
