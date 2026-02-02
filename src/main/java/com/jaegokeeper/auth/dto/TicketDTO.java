package com.jaegokeeper.auth.dto;

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

}
