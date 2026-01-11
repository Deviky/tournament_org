package com.deviky.Participant_Service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreatePlayerRequest {
    private Long id;
    private String nickname;
    private String links;
    private List<TeamSummaryDto> teams;
}
