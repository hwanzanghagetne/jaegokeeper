package com.jaegokeeper.ddan.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginTarget {

    private int userId;
    private int storeId;
    private String userName;
    private Boolean isActive;

}
