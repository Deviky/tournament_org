package com.deviky.Match_Service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Tournament {
    Long id;
    Long organizerId;
    Integer gameId;
    String name;
    String description;
    Integer minTeams;
    Integer maxTeams;
    String type;
    String status;
    LocalDateTime startAt;
    LocalDateTime endAt;
}
