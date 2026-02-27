package com.deviky.Tournament_Service.tournament_core.dto;

import com.deviky.Tournament_Service.bracket.bracket_core.models.Bracket;
import com.deviky.Tournament_Service.tournament_core.models.TournamentStatus;
import com.deviky.Tournament_Service.tournament_core.models.TournamentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TournamentDto {
    Long id;
    Long organizerId;
    Integer gameId;
    String name;
    String description;
    Integer minTeams;
    Integer maxTeams;
    Bracket bracket;
    TournamentType type;
    TournamentStatus status;
    LocalDateTime startAt;
    LocalDateTime endAt;
    Organization organization;
    List<Team> teams;
}
