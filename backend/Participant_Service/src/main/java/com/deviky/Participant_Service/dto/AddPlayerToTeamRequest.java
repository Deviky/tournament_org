package com.deviky.Participant_Service.dto;

import lombok.Data;

@Data
public class AddPlayerToTeamRequest {
    private Long playerId;
    private Long teamId;
}
