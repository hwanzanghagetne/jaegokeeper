package com.jaegokeeper.auth.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialRequest {
    String provider;
    String accessToken;
}
