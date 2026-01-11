package com.deviky.Game_Service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Player {
    private Long id;
    private String nickname;
    private List<PlayerGameInfo> games;
}
