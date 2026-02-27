package com.deviky.Tournament_Service.tournament_core.models;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TournamentTeamId implements Serializable {

    private Long tournamentId;
    private Long teamId;
}
