package com.deviky.Tournament_Service.bracket.bracket_core.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BracketSlot {
    Long teamId;
    Long refMatchId;
    MatchTeamResult matchTeamResult;
}
