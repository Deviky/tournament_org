package com.deviky.Auth_Service.dto;

import lombok.*;

import java.util.List;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RegisterPlayerRequest extends LoginRequest{
    private String nickname;
    private List<PlayerGameInfo> games;
}
