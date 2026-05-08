package com.deviky.Match_Service.dto;

import com.deviky.Match_Service.models.MatchTeamResult;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MatchTeamDto {
    private Long id;
    private Integer gameId;
    private String name;
    private String status;
    private String type;
    private MatchTeamResult result;
    private List<TeamPlayer> players;
}