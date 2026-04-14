package com.deviky.Auth_Service.dto;

import lombok.*;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ModeratorCreateRequest extends LoginRequest{
    private String nickname;
}
