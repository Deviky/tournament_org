package com.deviky.Match_Service.models;

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
@Table(schema = "match", name = "match")
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;
    @Nonnull
    @Column(name = "tournament_id")
    Long tournamentId;
    @Nonnull
    MatchStatus status;
    String links;
    @Column(name = "start_at")
    LocalDateTime startAt;
    @Column(name = "end_at")
    LocalDateTime endAt;
    @OneToMany(mappedBy = "match", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MatchTeam> matchTeams = new ArrayList<>();
}
