package com.deviky.Tournament_Service.tournament_core.dto;


import com.deviky.Tournament_Service.tournament_core.models.TournamentStatus;
import com.deviky.Tournament_Service.tournament_core.models.TournamentType;
import jakarta.annotation.Nonnull;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TournamentCreateDto {
    Long organizerId;
    Integer gameId;
    String name;
    String description;
    Integer minTeams;
    Integer maxTeams;
    TournamentType type;
    LocalDateTime startAt;
    LocalDateTime endAt;
}
