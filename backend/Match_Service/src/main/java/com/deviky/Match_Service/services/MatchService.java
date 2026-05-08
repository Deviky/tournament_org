package com.deviky.Match_Service.services;

import com.deviky.Match_Service.dto.ApiResponse;
import com.deviky.Match_Service.dto.CreateMatchDto;
import com.deviky.Match_Service.dto.MatchDto;
import com.deviky.Match_Service.dto.MatchResultDto;
import com.deviky.Match_Service.dto.MatchTeamDto;
import com.deviky.Match_Service.dto.Team;
import com.deviky.Match_Service.dto.UpdateMatchDto;
import com.deviky.Match_Service.models.Match;
import com.deviky.Match_Service.models.MatchStatus;
import com.deviky.Match_Service.models.MatchTeam;
import com.deviky.Match_Service.models.MatchTeamId;
import com.deviky.Match_Service.models.MatchTeamResult;
import com.deviky.Match_Service.repositories.MatchRepository;
import com.deviky.Match_Service.repositories.MatchTeamRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final MatchTeamRepository matchTeamRepository;
    private final ParticipantClientService participantClientService;
    private final TournamentClientService tournamentClientService;

    private ApiResponse<MatchDto> createMatchImpl(CreateMatchDto createMatchDto) {
        List<Long> teamIds = createMatchDto.getTeamIds() != null
                ? createMatchDto.getTeamIds()
                : new ArrayList<>();

        if (teamIds.size() > 2) {
            return new ApiResponse<>("В матче нельзя указать более 2 участников", null, true);
        }

        Set<Long> uniqueTeamIds = new HashSet<>(teamIds);
        if (uniqueTeamIds.size() != teamIds.size()) {
            return new ApiResponse<>("Участники не должны дублироваться в матче", null, true);
        }

        Match match = Match.builder()
                .tournamentId(createMatchDto.getTournamentId())
                .links(createMatchDto.getLinks())
                .status(MatchStatus.COMING)
                .startAt(createMatchDto.getStartAt())
                .endAt(createMatchDto.getEndAt())
                .build();

        Match savedMatch = matchRepository.save(match);

        List<MatchTeam> matchTeams = new ArrayList<>();
        for (Long teamId : teamIds) {
            MatchTeamId matchTeamId = MatchTeamId.builder()
                    .matchId(savedMatch.getId())
                    .teamId(teamId)
                    .build();

            MatchTeam matchTeam = MatchTeam.builder()
                    .id(matchTeamId)
                    .match(savedMatch)
                    .status("ACTIVE")
                    .result(MatchTeamResult.NOT_PLAYED)
                    .build();

            matchTeams.add(matchTeam);
        }

        if (!matchTeams.isEmpty()) {
            matchTeamRepository.saveAll(matchTeams);
        }

        MatchDto matchDto = MatchDto.builder()
                .id(savedMatch.getId())
                .tournamentId(savedMatch.getTournamentId())
                .status(savedMatch.getStatus())
                .links(savedMatch.getLinks())
                .startAt(savedMatch.getStartAt())
                .endAt(savedMatch.getEndAt())
                .teams(new ArrayList<>())
                .build();

        return new ApiResponse<>("Матч успешно создан", matchDto, false);
    }

    @Transactional
    public ApiResponse<Map<Long, MatchDto>> createMatchesByBracket(Map<Long, CreateMatchDto> mapMatchesDto) {
        try {
            Map<Long, MatchDto> createdMatches = new HashMap<>();

            for (Map.Entry<Long, CreateMatchDto> entry : mapMatchesDto.entrySet()) {
                ApiResponse<MatchDto> matchDto = createMatchImpl(entry.getValue());
                if (matchDto.isError()) {
                    return new ApiResponse<>(matchDto.getMessage(), null, true);
                }

                createdMatches.put(entry.getKey(), matchDto.getData());
            }

            return new ApiResponse<>("", createdMatches, false);
        } catch (Exception e) {
            return new ApiResponse<>(e.getMessage(), null, true);
        }
    }

    public ApiResponse<Void> updateMatch(UpdateMatchDto updateMatchDto, Long organizerId) {
        try {
            Match match = matchRepository.findById(updateMatchDto.getMatchId())
                    .orElseThrow(() -> new Exception("Матч не найден"));

            ApiResponse<Void> tournamentResponse =
                    tournamentClientService.checkTournamentInfo(match.getTournamentId(), organizerId);

            if (tournamentResponse.isError()) {
                return new ApiResponse<>(tournamentResponse.getMessage(), null, true);
            }

            if (match.getStatus() != MatchStatus.COMING) {
                return new ApiResponse<>("Невозможно обновить матч с текущим статусом", null, true);
            }

            match.setLinks(updateMatchDto.getLinks());
            match.setStartAt(updateMatchDto.getStartAt());
            matchRepository.save(match);

            return new ApiResponse<>("Матч обновлён", null, false);
        } catch (Exception e) {
            return new ApiResponse<>(e.getMessage(), null, true);
        }
    }

    public ApiResponse<MatchDto> getMatch(Long matchId) {
        try {
            Match match = matchRepository.findById(matchId)
                    .orElseThrow(() -> new Exception("Матч не найден"));

            List<MatchTeamDto> teams = resolveMatchTeams(match.getMatchTeams());

            MatchDto matchDto = MatchDto.builder()
                    .id(match.getId())
                    .tournamentId(match.getTournamentId())
                    .status(match.getStatus())
                    .links(match.getLinks())
                    .startAt(match.getStartAt())
                    .endAt(match.getEndAt())
                    .teams(teams)
                    .build();

            return new ApiResponse<>("", matchDto, false);
        } catch (Exception e) {
            return new ApiResponse<>(e.getMessage(), null, true);
        }
    }

    public ApiResponse<List<MatchDto>> getMatchesByTournament(Long tournamentId) {
        try {
            List<Match> matches = matchRepository.findByTournamentId(tournamentId);

            if (matches.isEmpty()) {
                return new ApiResponse<>("Матчи для данного турнира не найдены", new ArrayList<>(), false);
            }

            Set<Long> allTeamIds = matches.stream()
                    .flatMap(match -> match.getMatchTeams().stream())
                    .map(mt -> mt.getId().getTeamId())
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());

            Map<Long, Team> teamMap = loadTeamsMap(new ArrayList<>(allTeamIds));

            List<MatchDto> matchDtos = matches.stream()
                    .map(match -> MatchDto.builder()
                            .id(match.getId())
                            .tournamentId(match.getTournamentId())
                            .status(match.getStatus())
                            .links(match.getLinks())
                            .startAt(match.getStartAt())
                            .endAt(match.getEndAt())
                            .teams(toMatchTeamDtos(match.getMatchTeams(), teamMap))
                            .build())
                    .toList();

            return new ApiResponse<>("Матчи успешно получены", matchDtos, false);
        } catch (Exception e) {
            return new ApiResponse<>("Ошибка при получении матчей: " + e.getMessage(), null, true);
        }
    }

    public ApiResponse<Void> startMatch(Long matchId, Long organizerId) {
        try {
            Match match = matchRepository.findById(matchId)
                    .orElseThrow(() -> new Exception("Матч не найден"));

            ApiResponse<Void> tournamentResponse =
                    tournamentClientService.checkTournamentInfo(match.getTournamentId(), organizerId);

            if (tournamentResponse.isError()) {
                return new ApiResponse<>(tournamentResponse.getMessage(), null, true);
            }

            if (match.getStatus() != MatchStatus.COMING) {
                return new ApiResponse<>("Невозможно начать матч с текущим статусом", null, true);
            }

            if (match.getStartAt() == null) {
                match.setStartAt(LocalDateTime.now());
            }

            match.setStatus(MatchStatus.RUNNING);
            matchRepository.save(match);

            return new ApiResponse<>("Матч обновлён", null, false);
        } catch (Exception e) {
            return new ApiResponse<>(e.getMessage(), null, true);
        }
    }

    @Transactional
    public ApiResponse<Void> finishMatch(MatchResultDto matchResult, Long organizerId) {
        try {
            Match match = matchRepository.findById(matchResult.getMatchId())
                    .orElseThrow(() -> new Exception("Матч не найден"));

            ApiResponse<Void> tournamentResponse =
                    tournamentClientService.checkTournamentInfo(match.getTournamentId(), organizerId);

            if (tournamentResponse.isError()) {
                return new ApiResponse<>(tournamentResponse.getMessage(), null, true);
            }

            if (match.getStatus() != MatchStatus.RUNNING) {
                return new ApiResponse<>("Невозможно завершить матч с текущим статусом", null, true);
            }

            List<MatchTeam> matchTeams = match.getMatchTeams();
            boolean allTeamsHaveResult = matchTeams.stream()
                    .allMatch(team -> matchResult.getTeamToMatchResult().containsKey(team.getId().getTeamId()));

            if (!allTeamsHaveResult) {
                return new ApiResponse<>("Не для всех команд указан результат", null, true);
            }

            long winnersCount = matchResult.getTeamToMatchResult().values().stream()
                    .filter(result -> result == MatchTeamResult.WINNER)
                    .count();

            if (winnersCount != 1) {
                return new ApiResponse<>("В матче должен быть ровно один победитель", null, true);
            }

            matchTeams.forEach(matchTeam ->
                    matchTeam.setResult(matchResult.getTeamToMatchResult().get(matchTeam.getId().getTeamId()))
            );

            matchTeamRepository.saveAll(matchTeams);
            match.setStatus(MatchStatus.FINISHED);
            match.setEndAt(LocalDateTime.now());
            matchRepository.save(match);

            ApiResponse<Void> updateBracketResponse =
                    tournamentClientService.updateBracket(match.getTournamentId(), matchResult);

            if (updateBracketResponse == null || updateBracketResponse.isError()) {
                throw new RuntimeException(
                        updateBracketResponse != null
                                ? updateBracketResponse.getMessage()
                                : "Не удалось обновить сетку турнира"
                );
            }

            return new ApiResponse<>("Матч обновлён", null, false);
        } catch (Exception e) {
            return new ApiResponse<>(e.getMessage(), null, true);
        }
    }

    @Transactional
    public ApiResponse<Void> cancelMatch(Long matchId, Long organizerId) {
        try {
            Match match = matchRepository.findById(matchId)
                    .orElseThrow(() -> new Exception("Матч не найден"));

            ApiResponse<Void> tournamentResponse =
                    tournamentClientService.checkTournamentInfo(match.getTournamentId(), organizerId);

            if (tournamentResponse.isError()) {
                return new ApiResponse<>(tournamentResponse.getMessage(), null, true);
            }

            if (match.getStatus() == MatchStatus.FINISHED) {
                return new ApiResponse<>("Матч нельзя отменить, поскольку он уже завершён", null, true);
            }

            if (match.getStatus() == MatchStatus.CANCELED) {
                return new ApiResponse<>("Матч уже отменён", null, true);
            }

            match.setStatus(MatchStatus.CANCELED);

            ApiResponse<Void> updateBracketResponse =
                    tournamentClientService.cancelMatchUpdateBracket(match.getTournamentId(), matchId);

            if (updateBracketResponse.isError()) {
                throw new RuntimeException("Не удалось обновить сетку турнира: " + updateBracketResponse.getMessage());
            }

            matchRepository.save(match);
            return new ApiResponse<>("Матч обновлён", null, false);
        } catch (Exception e) {
            return new ApiResponse<>(e.getMessage(), null, true);
        }
    }

    public ApiResponse<Void> cancelTournamentMatches(Long tournamentId) {
        try {
            List<Match> matches = matchRepository.findByTournamentId(tournamentId);
            List<Match> readyMatches = new ArrayList<>();

            for (Match match : matches) {
                if (match.getStatus() == MatchStatus.FINISHED || match.getStatus() == MatchStatus.CANCELED) {
                    continue;
                }

                match.setStatus(MatchStatus.CANCELED);
                readyMatches.add(match);
            }

            matchRepository.saveAll(readyMatches);
            return new ApiResponse<>("Матчи обновлены", null, false);
        } catch (Exception e) {
            return new ApiResponse<>(e.getMessage(), null, true);
        }
    }

    private List<MatchTeamDto> resolveMatchTeams(List<MatchTeam> matchTeams) {
        List<Long> teamIds = matchTeams.stream()
                .map(mt -> mt.getId().getTeamId())
                .filter(Objects::nonNull)
                .toList();

        if (teamIds.isEmpty()) {
            return new ArrayList<>();
        }

        Map<Long, Team> teamMap = loadTeamsMap(teamIds);
        return toMatchTeamDtos(matchTeams, teamMap);
    }

    private Map<Long, Team> loadTeamsMap(List<Long> teamIds) {
        if (teamIds == null || teamIds.isEmpty()) {
            return new HashMap<>();
        }

        ApiResponse<List<Team>> teamsResponse = participantClientService.getTeams(teamIds);
        if (teamsResponse.isError()) {
            throw new RuntimeException("Не удалось получить команды: " + teamsResponse.getMessage());
        }

        List<Team> teams = teamsResponse.getData() != null ? teamsResponse.getData() : new ArrayList<>();
        return teams.stream().collect(Collectors.toMap(Team::getId, team -> team, (left, right) -> left));
    }

    private List<MatchTeamDto> toMatchTeamDtos(List<MatchTeam> matchTeams, Map<Long, Team> teamMap) {
        return matchTeams.stream()
                .map(matchTeam -> {
                    Long teamId = matchTeam.getId().getTeamId();
                    Team team = teamMap.get(teamId);

                    if (team == null) {
                        return null;
                    }

                    return new MatchTeamDto(
                            team.getId(),
                            team.getGameId(),
                            team.getName(),
                            team.getStatus(),
                            team.getType(),
                            matchTeam.getResult(),
                            team.getPlayers()
                    );
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
