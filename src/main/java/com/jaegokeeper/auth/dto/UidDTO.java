package com.jaegokeeper.auth.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UidDTO {

    private int authId;
    private int userId;
    private String provider;
    private String providerUid;
    private String passHash;

}
