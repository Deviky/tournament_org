package com.deviky.Match_Service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Team {
    private Long id;
    private Integer gameId;
    private String name;
    private String status;
    private String type;
    private List<TeamPlayer> players;
}