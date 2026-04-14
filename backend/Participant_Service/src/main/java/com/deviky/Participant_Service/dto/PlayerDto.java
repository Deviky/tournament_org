package com.deviky.Participant_Service.dto;

import com.deviky.Participant_Service.models.PlayerGameInfo;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PlayerDto {
    private Long id;
    private String nickname;
    private List<PlayerGameInfo> games;
    private List<TeamSummaryDto> teams;
    private List<PlayerStatisticDto> statistics;
}
