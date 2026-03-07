package com.deviky.Tournament_Service.bracket.bracket_core.models;

import lombok.Data;

import java.util.Map;

@Data
public class MatchResult {
    Long matchId;
    Map<Long, MatchTeamResult> teamsResults;
}
