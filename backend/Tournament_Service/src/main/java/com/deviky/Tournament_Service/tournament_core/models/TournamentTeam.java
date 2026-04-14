package com.deviky.Tournament_Service.tournament_core.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(schema = "tournament", name = "tournament_teams")
public class TournamentTeam {

    @EmbeddedId
    private TournamentTeamId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("tournamentId")
    @JoinColumn(name = "tournament_id")
    private Tournament tournament;
    @Column(name = "registered_at")
    private LocalDateTime registeredAt;
    @Enumerated(EnumType.STRING)
    private TournamentTeamStatus status;
}