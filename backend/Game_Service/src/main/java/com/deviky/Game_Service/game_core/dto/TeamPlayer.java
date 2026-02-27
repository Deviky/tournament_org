package com.deviky.Game_Service.game_core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamPlayer {
    private Long id;
    private String nickname;
    private boolean isCaptain;
    private String status;
}
