package com.deviky.Participant_Service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchDto{
        List<PlayerDto> players;
        List<TeamDto> teams;
}
