package com.deviky.Match_Service.dto;

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
    Map<Long, MatchTeamResult> teamToMatchResult;
}
