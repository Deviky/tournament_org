package com.deviky.Tournament_Service;

import com.deviky.Tournament_Service.bracket.bracket_algorithms.SINGLE_ELIMINATION.SingleEliminationAlgorithm;
import com.deviky.Tournament_Service.bracket.bracket_algorithms.SINGLE_ELIMINATION.SingleEliminationParams;
import com.deviky.Tournament_Service.bracket.bracket_core.models.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class SingleEliminationAlgorithmTest {

    private SingleEliminationAlgorithm algorithm;

    @BeforeEach
    void setUp() {
        algorithm = new SingleEliminationAlgorithm();
    }

    private SingleEliminationParams defaultParams(boolean shuffle) {
        SingleEliminationParams params = new SingleEliminationParams();
        params.setShuffle(shuffle);
        return params;
    }

    private void saveBracketToJson(Bracket bracket, String fileName) {
        try {
            File dir = new File("test-output");
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File file = new File(dir, fileName);

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(bracket.toJsonStr());
            }

            System.out.println("Saved JSON to: " + file.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void generate_shouldCreateBracket_correctStructure() {
        List<Long> teams = List.of(1L, 2L, 3L, 4L);

        Bracket bracket = algorithm.generate(teams, defaultParams(false));

        saveBracketToJson(bracket, "bracket_generate.json");

        assertNotNull(bracket);
        assertEquals(1, bracket.getBracketGroups().size());

        BracketGroup group = bracket.getBracketGroups().get(0);
        List<BracketMatch> matches = group.getMatches();

        // 4 команды -> 3 матча
        assertEquals(3, matches.size());

        BracketMatch m1 = matches.get(0);
        BracketMatch m2 = matches.get(1);

        assertEquals(1L, m1.getSlots().get(0).getTeamId());
        assertEquals(2L, m1.getSlots().get(1).getTeamId());

        assertEquals(3L, m2.getSlots().get(0).getTeamId());
        assertEquals(4L, m2.getSlots().get(1).getTeamId());

        // финал
        BracketMatch finalMatch = matches.get(2);
        assertNull(finalMatch.getSlots().get(0).getTeamId());
        assertNotNull(finalMatch.getSlots().get(0).getRefMatchId());
    }

    @Test
    void generate_shouldThrow_ifNotPowerOfTwo() {
        List<Long> teams = List.of(1L, 2L, 3L);

        assertThrows(IllegalArgumentException.class, () ->
                algorithm.generate(teams, defaultParams(false))
        );
    }

    @Test
    void update_shouldAdvanceWinner() {
        List<Long> teams = List.of(1L, 2L, 3L, 4L);
        Bracket bracket = algorithm.generate(teams, defaultParams(false));

        BracketMatch firstMatch = bracket.getBracketGroups().get(0).getMatches().get(0);

        Map<Long, MatchTeamResult> results = new HashMap<>();
        results.put(1L, MatchTeamResult.WINNER);
        results.put(2L, MatchTeamResult.LOSER);

        MatchResult matchResult = new MatchResult(firstMatch.getMatchId(), results);

        Bracket updated = algorithm.update(matchResult, bracket);

        saveBracketToJson(updated, "bracket_after_update.json");

        BracketMatch finalMatch = updated.getBracketGroups().get(0).getMatches().get(2);

        boolean found = finalMatch.getSlots().stream()
                .anyMatch(slot -> Objects.equals(slot.getTeamId(), 1L));

        assertTrue(found);
    }

    @Test
    void cancelMatch_shouldClearDependentSlots() {
        List<Long> teams = List.of(1L, 2L, 3L, 4L);
        Bracket bracket = algorithm.generate(teams, defaultParams(false));

        BracketMatch firstMatch = bracket.getBracketGroups().get(0).getMatches().get(0);

        // сначала продвигаем победителя
        Map<Long, MatchTeamResult> results = new HashMap<>();
        results.put(1L, MatchTeamResult.WINNER);
        results.put(2L, MatchTeamResult.LOSER);

        MatchResult matchResult = new MatchResult(firstMatch.getMatchId(), results);
        bracket = algorithm.update(matchResult, bracket);

        // затем отменяем матч
        bracket = algorithm.cancelMatch(firstMatch.getMatchId(), bracket);

        saveBracketToJson(bracket, "bracket_after_cancel.json");

        BracketMatch finalMatch = bracket.getBracketGroups().get(0).getMatches().get(2);

        boolean stillExists = finalMatch.getSlots().stream()
                .anyMatch(slot -> Objects.equals(slot.getTeamId(), 1L));

        assertFalse(stillExists);
    }
}