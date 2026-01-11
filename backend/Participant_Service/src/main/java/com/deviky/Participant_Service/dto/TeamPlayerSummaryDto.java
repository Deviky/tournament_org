package com.deviky.Participant_Service.dto;

import com.deviky.Participant_Service.models.TeamPlayerStatus;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamPlayerSummaryDto {
    private Long id;
    private String nickname;
    private String links;
    private boolean isCaptain;
    private TeamPlayerStatus status;
}
