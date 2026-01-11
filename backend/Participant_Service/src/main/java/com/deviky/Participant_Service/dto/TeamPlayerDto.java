package com.deviky.Participant_Service.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamPlayerDto {
    private Long playerId;
    private Long teamId;
    private String status;
    private boolean isCaptain;
}

