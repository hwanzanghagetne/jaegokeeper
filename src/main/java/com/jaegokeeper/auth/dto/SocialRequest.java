package com.jaegokeeper.auth.dto;

import com.jaegokeeper.auth.enums.Provider;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SocialRequest {
    Provider provider;
    String accessToken;
}
