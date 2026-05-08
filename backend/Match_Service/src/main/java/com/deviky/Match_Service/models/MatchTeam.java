package com.deviky.Match_Service.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(schema = "match", name = "match_teams")
public class MatchTeam {
    @EmbeddedId
    MatchTeamId id;

    @Column(name = "status", nullable = false)
    String status;

    @Convert(converter = MatchTeamResultConverter.class)
    @Column(name = "result")
    MatchTeamResult result;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("matchId")
    @JoinColumn(name = "match_id")
    private Match match;
}
