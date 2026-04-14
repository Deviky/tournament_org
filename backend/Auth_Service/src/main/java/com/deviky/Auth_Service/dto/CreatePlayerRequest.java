package com.deviky.Auth_Service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CreatePlayerRequest {
    private Long id;
    private String nickname;
    private List<PlayerGameInfo> games;
}