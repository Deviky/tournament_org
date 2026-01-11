package com.deviky.Participant_Service.models;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamPlayerId implements Serializable {

    @Column(name = "player_id")
    private Long playerId;

    @Column(name = "team_id")
    private Long teamId;
}