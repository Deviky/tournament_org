package com.deviky.Participant_Service.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(schema = "participant", name = "team_player")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamPlayer {

    @EmbeddedId
    private TeamPlayerId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("playerId")
    @JoinColumn(name = "player_id")
    private Player player;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("teamId")
    @JoinColumn(name = "team_id")
    private Team team;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TeamPlayerStatus status;

    @Column(name = "is_captain", nullable = false)
    private boolean isCaptain;
}
