package com.jaegokeeper.ddan.user.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class TicketDTO {

    private String ticketKey;
    private int userId;
    private String redirectUrl;
    private Date expiresAt;
    private String usedYn;

}
