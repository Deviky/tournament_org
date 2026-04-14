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
@Table(schema = "tournament", name = "tournament")
public class Tournament {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;
    @Nonnull
    @Column(name = "organizer_id")
    Long organizerId;
    @Nonnull
    Integer gameId;
    @Nonnull
    String name;
    @Nonnull
    String description;
    @Column(name = "min_teams")
    Integer minTeams;
    @Column(name = "max_teams")
    Integer maxTeams;
    @Column(name = "bracket", columnDefinition = "TEXT")
    String bracket;
    @Nonnull
    @Enumerated(EnumType.STRING)
    TournamentType type;
    @Nonnull
    @Enumerated(EnumType.STRING)
    TournamentStatus status;
    @Nonnull
    @Column(name = "start_at")
    LocalDateTime startAt;
    @Column(name = "end_at")
    LocalDateTime endAt;
    @OneToMany(
            mappedBy = "tournament",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.EAGER
    )
    private List<TournamentTeam> teams = new ArrayList<>();
}
