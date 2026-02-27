package com.deviky.Tournament_Service.tournament_core.dto;

import com.deviky.Tournament_Service.bracket.bracket_core.algorithm_base.AlgorithmParams;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TournamentGenerateBracketDto {
    String algorithmName;
    AlgorithmParams algorithmParams;
}
