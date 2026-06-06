package com.deviky.Match_Service.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.deviky.Match_Service.models.MatchTeamResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchResultDto {
    Long matchId;

    // Keep backward compatibility for callers using teamToMatchResult,
    // but serialize with the field name expected by Tournament Service.
    @JsonProperty("teamsResults")
    @JsonAlias("teamToMatchResult")
    Map<Long, MatchTeamResult> teamToMatchResult;
}
