package com.deviky.Participant_Service.dto;

import com.deviky.Participant_Service.models.TeamStatus;
import com.deviky.Participant_Service.models.TeamType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TeamDto {
    private Long id;
    private Integer gameId;
    private String name;
    private TeamStatus status;
    private TeamType type;
    private List<TeamPlayerSummaryDto> players;
}