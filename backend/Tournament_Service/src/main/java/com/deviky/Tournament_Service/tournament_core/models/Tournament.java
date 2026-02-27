package com.deviky.Tournament_Service.tournament_core.models;

import jakarta.annotation.Nonnull;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Tournament {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;
    @Nonnull
    Long organizerId;
    @Nonnull
    Integer gameId;
    @Nonnull
    String name;
    @Nonnull
    String description;
    Integer minTeams;
    Integer maxTeams;
    String bracket;
    @Nonnull
    TournamentType type;
    @Nonnull
    TournamentStatus status;
    LocalDateTime startAt;
    LocalDateTime endAt;
    @OneToMany(
            mappedBy = "tournament",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<TournamentTeam> teams = new ArrayList<>();
}
