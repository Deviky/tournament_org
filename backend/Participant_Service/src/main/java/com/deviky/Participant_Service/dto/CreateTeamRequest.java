package com.deviky.Participant_Service.dto;

import com.deviky.Participant_Service.models.TeamType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateTeamRequest {
    private Integer gameId;
    private String name;
    private TeamType type;
}