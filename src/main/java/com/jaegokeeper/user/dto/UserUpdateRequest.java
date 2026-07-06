package com.jaegokeeper.user.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserUpdateRequest {
    private int userId;

    @Email(message = "이메일 형식이 올바르지 않습니다")
    @Size(max = 255, message = "이메일은 255자 이하여야 합니다")
    private String userMail;

    @Size(max = 20, message = "전화번호는 20자 이하여야 합니다")
    private String userPhone;
}
