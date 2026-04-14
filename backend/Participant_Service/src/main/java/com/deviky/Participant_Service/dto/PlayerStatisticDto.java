package com.deviky.Participant_Service.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerStatisticDto {
    Integer gameId;
    List<JsonNode> platformStatistics;
}
