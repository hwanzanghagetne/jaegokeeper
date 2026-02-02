package com.jaegokeeper.auth.dto;

import lombok.*;

@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {
    private int userId;
    private int storeId;
    private String name;
    private String email;
}