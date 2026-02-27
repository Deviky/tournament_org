package com.deviky.Tournament_Service.tournament_core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Player {
    private Long id;
    private String nickname;
    private boolean isCaptain;
    private String status;
}
