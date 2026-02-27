package com.deviky.Tournament_Service.bracket.bracket_algorithms.SINGLE_ELIMINATION;

import com.deviky.Tournament_Service.bracket.bracket_core.algorithm_base.AlgorithmParams;
import com.deviky.Tournament_Service.bracket.bracket_core.algorithm_base.BracketAlgorithm;
import com.deviky.Tournament_Service.bracket.bracket_core.models.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


@Component
public class SingleEliminationAlgorithm implements BracketAlgorithm {

    @Override
    public Bracket generate(List<Long> teamIds, AlgorithmParams params) {
        SingleEliminationParams p = (SingleEliminationParams) params;
        p.validate();

        int n = teamIds.size();
        if ((n & (n - 1)) != 0) {
            throw new IllegalArgumentException("Количество команд должно являться степенью двойки");
        }

        List<Long> teams = new ArrayList<>(teamIds);
        if (p.getShuffle()) {
            Collections.shuffle(teams);
        }

        List<BracketMatch> allMatches = new ArrayList<>();
        long matchId = 1;

        // ---------- Round 1 ----------
        List<BracketMatch> prevRound = new ArrayList<>();

        for (int i = 0; i < teams.size(); i += 2) {
            BracketMatch match = new BracketMatch(
                    matchId++,
                    List.of(
                            new BracketSlot(teams.get(i), null, null),
                            new BracketSlot(teams.get(i + 1), null, null)
                    )
            );
            prevRound.add(match);
            allMatches.add(match);
        }

        // ---------- Next rounds ----------
        while (prevRound.size() > 1) {
            List<BracketMatch> currentRound = new ArrayList<>();

            for (int i = 0; i < prevRound.size(); i += 2) {
                BracketMatch m1 = prevRound.get(i);
                BracketMatch m2 = prevRound.get(i + 1);

                BracketMatch match = new BracketMatch(
                        matchId++,
                        List.of(
                                new BracketSlot(null, m1.getMatchId(), RequiredMatchResult.WINNER),
                                new BracketSlot(null, m2.getMatchId(), RequiredMatchResult.WINNER)
                        )
                );

                currentRound.add(match);
                allMatches.add(match);
            }

            prevRound = currentRound;
        }

        // Вся сетка в одной подгруппе
        BracketGroup group = new BracketGroup("Сетка", allMatches);

        Bracket bracket = new Bracket(getVersion(),getType(),List.of(group));
        return bracket;
    }

    @Override
    public Bracket update(MatchResult matchResult, Bracket bracket) {
        // 1. Определяем победителя матча
        Long winnerId = null;
        for (Map.Entry<Long, RequiredMatchResult> entry : matchResult.getTeamsResult().entrySet()) {
            if (entry.getValue() == RequiredMatchResult.WINNER) {
                winnerId = entry.getKey();
                break;
            }
        }

        if (winnerId == null) {
            throw new IllegalArgumentException("MatchResult must contain a winner");
        }

        // 2. Обновляем слот в сетке
        for (BracketGroup group : bracket.getBracketGroups()) {
            for (BracketMatch match : group.getMatches()) {
                for (BracketSlot slot : match.getSlots()) {
                    if (slot.getRefMatchId() != null && slot.getRefMatchId().equals(matchResult.getMatchId()) && slot.getRequiredMatchResult().equals(RequiredMatchResult.WINNER)) {
                        slot.setTeamId(winnerId);
                    }
                }
            }
        }

        return bracket;
    }

    @Override
    public String getType() {
        return "SINGLE_ELIMINATION";
    }

    @Override
    public String getVersion() {
        return "1.0.0";
    }

    @Override
    public Class<? extends AlgorithmParams> getAlgorithmParamsClass() {
        return SingleEliminationParams.class;
    }
}