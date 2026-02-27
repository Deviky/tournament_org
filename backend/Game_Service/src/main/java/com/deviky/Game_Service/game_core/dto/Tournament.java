package com.deviky.Game_Service.game_core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Tournament {
    Long id;
    Long organizerId;
    Integer gameId;
    String name;
    String description;
    Integer minTeams;
    Integer maxTeams;
    String bracket;
    String type;
    String status;
    LocalDateTime startAt;
    LocalDateTime endAt;
    Organization organization;
    List<Team> teams;
}
