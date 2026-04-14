package com.deviky.Tournament_Service.tournament_core.dto;


import com.deviky.Tournament_Service.tournament_core.models.TournamentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TournamentUpdateDto {
    String description;
    Integer minTeams;
    Integer maxTeams;
    TournamentType type;
    LocalDateTime startAt;
    LocalDateTime endAt;
}
