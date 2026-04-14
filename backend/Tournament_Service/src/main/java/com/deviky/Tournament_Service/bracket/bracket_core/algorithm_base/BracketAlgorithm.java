package com.deviky.Tournament_Service.bracket.bracket_core.algorithm_base;

import com.deviky.Tournament_Service.bracket.bracket_core.models.Bracket;
import com.deviky.Tournament_Service.bracket.bracket_core.models.BracketGroup;
import com.deviky.Tournament_Service.bracket.bracket_core.models.MatchResult;

import java.util.List;

public interface BracketAlgorithm {

    Bracket generate(List<Long> teamIds, AlgorithmParams params);

    Bracket update(MatchResult matchResult, Bracket bracket);
    Bracket cancelMatch(Long matchId, Bracket bracket);

    String getType();
    String getVersion();

    Class<? extends AlgorithmParams> getAlgorithmParamsClass();
}

