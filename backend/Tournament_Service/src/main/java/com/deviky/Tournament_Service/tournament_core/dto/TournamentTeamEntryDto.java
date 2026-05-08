package com.deviky.Tournament_Service.tournament_core.dto;

import com.deviky.Tournament_Service.tournament_core.models.TournamentTeamStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TournamentTeamEntryDto {
    private Long tournamentId;
    private Long teamId;
    private LocalDateTime registeredAt;
    private TournamentTeamStatus status;
    private Team team;
}
