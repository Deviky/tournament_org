package com.deviky.Match_Service.models;

import jakarta.persistence.Column;
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
public class MatchTeamId implements Serializable {
    @Column(name = "match_id")
    Long matchId;
    @Column(name = "team_id")
    Long teamId;
}
