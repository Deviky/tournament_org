package com.deviky.Tournament_Service.tournament_core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Team {
    private Long id;
    private Integer gameId;
    private String name;
    private String status;
    private String type;
    private List<Player> players;
}
