package com.deviky.Match_Service.services;


import com.deviky.Match_Service.dto.*;
import com.deviky.Match_Service.models.*;
import com.deviky.Match_Service.repositories.MatchRepository;
import com.deviky.Match_Service.repositories.MatchTeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchService {

    private final MatchRepository matchRepository;
    private final MatchTeamRepository matchTeamRepository;
    private final ParticipantClientService participantClientService;
    private final TournamentClientService tournamentClientService;


    public ApiResponse<MatchDto> createMatch(CreateMatchDto createMatchDto, Long organizerId){
        ApiResponse<Void> tournamentResponse = tournamentClientService.checkTournamentInfo(createMatchDto.getTournamentId(), organizerId);

        if (tournamentResponse.isError())
            return new ApiResponse<>(tournamentResponse.getMessage(), null, true);

        ApiResponse<MatchDto> matchDtoResponse = createMatchImpl(createMatchDto);
        if (matchDtoResponse.isError())
            return matchDtoResponse;

        MatchDto matchDto = matchDtoResponse.getData();

        ApiResponse<List<Team>> teamsResponse =  participantClientService.getTeams(createMatchDto.getTeamIds());

        if (teamsResponse.isError()) {
            matchDtoResponse.setMessage("Не удалось получить сведения о команде");
            matchDtoResponse.setError(true);
            return matchDtoResponse;
        }

        List<Team> teams = teamsResponse.getData();

        matchDto.setTeams(teams);

        matchDtoResponse.setData(matchDto);

        return matchDtoResponse;
    }

    private ApiResponse<MatchDto> createMatchImpl(CreateMatchDto createMatchDto) {
        try {
            // Валидация входных данных
            if (createMatchDto.getTeamIds() == null || createMatchDto.getTeamIds().size() < 2) {
                return new ApiResponse<>("Для матча необходимо минимум 2 участника", null, true);
            }

            // Проверяем на дубликаты команд
            Set<Long> uniqueTeamIds = new HashSet<>(createMatchDto.getTeamIds());
            if (uniqueTeamIds.size() != createMatchDto.getTeamIds().size()) {
                return new ApiResponse<>("Участники не должны дублироваться в матче", null, true);
            }

            // Создание матча
            Match match = Match.builder()
                    .tournamentId(createMatchDto.getTournamentId())
                    .links(createMatchDto.getLinks())
                    .status(MatchStatus.COMING)
                    .startAt(createMatchDto.getStartAt())
                    .endAt(createMatchDto.getEndAt())
                    .build();

            // Сохраняем матч
            Match savedMatch = matchRepository.save(match);

            // Создаем связи с командами
            List<MatchTeam> matchTeams = new ArrayList<>();
            for (Long teamId : createMatchDto.getTeamIds()) {
                MatchTeamId matchTeamId = MatchTeamId.builder()
                        .matchId(savedMatch.getId())
                        .teamId(teamId)
                        .build();

                MatchTeam matchTeam = MatchTeam.builder()
                        .id(matchTeamId)
                        .match(savedMatch)
                        .result(MatchTeamResult.NOT_PLAYED)
                        .build();

                matchTeams.add(matchTeam);
            }

            // Сохраняем все связи
            matchTeamRepository.saveAll(matchTeams);

            // Формируем ответ
            MatchDto matchDto = MatchDto.builder()
                    .id(savedMatch.getId())
                    .tournamentId(savedMatch.getTournamentId())
                    .status(savedMatch.getStatus())
                    .links(savedMatch.getLinks())
                    .startAt(savedMatch.getStartAt())
                    .endAt(savedMatch.getEndAt())
                    .build();

            return new ApiResponse<>("Матч успешно создан", matchDto, false);
        }
        catch (Exception e) {
            return new ApiResponse<>(e.getMessage(), null, true);
        }
    }

    public ApiResponse<Map<Long, MatchDto>> createMatchesByBracket(Map<Long, CreateMatchDto> mapMatchesDto){
        try {

            Map<Long, MatchDto> createdMatches = new HashMap<>();

            for (Long preMatchId : mapMatchesDto.keySet()) {
                CreateMatchDto createMatchDto = mapMatchesDto.get(preMatchId);
                ApiResponse<MatchDto> matchDto = createMatchImpl(createMatchDto);
                if (matchDto.isError())
                    return new ApiResponse<>(matchDto.getMessage(), null, true);

                createdMatches.put(preMatchId, matchDto.getData());
            }

            return new ApiResponse<>("", createdMatches, false);
        }
        catch (Exception e) {
            return new ApiResponse<>( e.getMessage(), null, true);
        }
    }

    public ApiResponse<Void> updateMatch(UpdateMatchDto updateMatchDto, Long organizerId){
        try {
            Match match = matchRepository.findById(updateMatchDto.getMatchId()).orElseThrow(() -> new Exception("Матч не найден"));

            ApiResponse<Void> tournamentResponse = tournamentClientService.checkTournamentInfo(match.getTournamentId(), organizerId);

            if (tournamentResponse.isError())
                return new ApiResponse<>(tournamentResponse.getMessage(), null, true);

            if (!match.getStatus().equals(MatchStatus.COMING))
                return new ApiResponse<>("Невохможно обновить матч с текущим статусом", null, true);

            match.setLinks(updateMatchDto.getLinks());
            match.setStartAt(updateMatchDto.getStartAt());
            match.setEndAt(updateMatchDto.getEndAt());
            matchRepository.save(match);
            return new ApiResponse<>("Матч обновлён", null, false);
        }
        catch (Exception e){
            return new ApiResponse<>(e.getMessage(), null, true);
        }
    }

    public ApiResponse<MatchDto> getMatch(Long matchId){
        try {
            Match match = matchRepository.findById(matchId).orElseThrow(() -> new Exception("Матч не найден"));

            List<Long> teamsIds = match.getMatchTeams().stream()
                    .map(matchTeam -> matchTeam.getId().getTeamId())  // Предполагая, что teamId есть в MatchTeamId
                    .toList();

            ApiResponse<List<Team>> teamsResponse =  participantClientService.getTeams(teamsIds);

            if (teamsResponse.isError()) {
                return new ApiResponse<>("Не удалось получить сведения о командах в матче", null, true );
            }

            List<Team> teams = teamsResponse.getData();


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
        }
        catch (Exception e){
            return new ApiResponse<>(e.getMessage(), null, true);
        }
    }

    public ApiResponse<List<MatchDto>> getMatchesByTournament(Long tournamentId) {
        try {
            List<Match> matches = matchRepository.findByTournamentId(tournamentId);

            if (matches.isEmpty()) {
                return new ApiResponse<>("Матчи для данного турнира не найдены", new ArrayList<>(), false);
            }

            // Собираем все ID команд
            Set<Long> allTeamIds = matches.stream()
                    .flatMap(match -> match.getMatchTeams().stream())
                    .map(matchTeam -> matchTeam.getId().getTeamId())
                    .collect(Collectors.toSet());

            // Получаем информацию о командах
            ApiResponse<List<Team>> teamsResponse = participantClientService.getTeams(new ArrayList<>(allTeamIds));

            Map<Long, Team> teamsMap = teamsResponse.isError() ? new HashMap<>() :
                    teamsResponse.getData().stream()
                            .collect(Collectors.toMap(Team::getId, team -> team));

            // Преобразуем в DTO
            List<MatchDto> matchDtos = matches.stream()
                    .map(match -> {
                        List<Team> matchTeams = match.getMatchTeams().stream()
                                .map(matchTeam -> teamsMap.get(matchTeam.getId().getTeamId()))
                                .filter(Objects::nonNull)
                                .collect(Collectors.toList());

                        return MatchDto.builder()
                                .id(match.getId())
                                .tournamentId(match.getTournamentId())
                                .status(match.getStatus())
                                .links(match.getLinks())
                                .startAt(match.getStartAt())
                                .endAt(match.getEndAt())
                                .teams(matchTeams)
                                .build();
                    })
                    .collect(Collectors.toList());

            return new ApiResponse<>("Матчи успешно получены", matchDtos, false);

        } catch (Exception e) {
            return new ApiResponse<>("Ошибка при получении матчей: " + e.getMessage(), null, true);
        }
    }

    public ApiResponse<Void> startMatch(Long matchId, Long organizerId){
        try {
            Match match = matchRepository.findById(matchId).orElseThrow(() -> new Exception("Матч не найден"));

            ApiResponse<Void> tournamentResponse = tournamentClientService.checkTournamentInfo(match.getTournamentId(), organizerId);

            if (tournamentResponse.isError())
                return new ApiResponse<>(tournamentResponse.getMessage(), null, true);

            if (!match.getStatus().equals(MatchStatus.COMING))
                return new ApiResponse<>("Невозможно начать матч с текущим статусом", null, true);

            match.setStatus(MatchStatus.RUNNING);
            matchRepository.save(match);
            return new ApiResponse<>("Матч обновлён", null, false);
        }
        catch (Exception e){
            return new ApiResponse<>(e.getMessage(), null, true);
        }
    }

    public ApiResponse<Void> finishMatch(Long matchId, Long organizerId){
        try {
            Match match = matchRepository.findById(matchId).orElseThrow(() -> new Exception("Матч не найден"));

            ApiResponse<Void> tournamentResponse = tournamentClientService.checkTournamentInfo(match.getTournamentId(), organizerId);

            if (tournamentResponse.isError())
                return new ApiResponse<>(tournamentResponse.getMessage(), null, true);

            if (!match.getStatus().equals(MatchStatus.RUNNING))
                return new ApiResponse<>("Невозможно завершить матч с текущим статусом", null, true);

            match.setStatus(MatchStatus.FINISHED);
            matchRepository.save(match);
            return new ApiResponse<>("Матч обновлён", null, false);
        }
        catch (Exception e){
            return new ApiResponse<>(e.getMessage(), null, true);
        }
    }
    public ApiResponse<Void> cancelMatch(Long matchId, Long organizerId){
        try {
            Match match = matchRepository.findById(matchId).orElseThrow(() -> new Exception("Матч не найден"));

            ApiResponse<Void> tournamentResponse = tournamentClientService.checkTournamentInfo(match.getTournamentId(), organizerId);

            if (tournamentResponse.isError())
                return new ApiResponse<>(tournamentResponse.getMessage(), null, true);

            if (match.getStatus().equals(MatchStatus.FINISHED))
                return new ApiResponse<>("Матч нельзя отменить поскольку он уже завершён", null, true);

            else if (match.getStatus().equals(MatchStatus.CANCELED))
                return new ApiResponse<>("Матч уже отменён", null, true);

            match.setStatus(MatchStatus.CANCELED);
            matchRepository.save(match);
            return new ApiResponse<>("Матч обновлён", null, false);
        }
        catch (Exception e){
            return new ApiResponse<>(e.getMessage(), null, true);
        }
    }

    public ApiResponse<Void> cancelTournamentMatches(Long tournamentId, Long organizerId){
        try {
            ApiResponse<Void> tournamentResponse = tournamentClientService.checkTournamentInfo(tournamentId, organizerId);

            if (tournamentResponse.isError())
                return new ApiResponse<>(tournamentResponse.getMessage(), null, true);

            List<Match> matches = matchRepository.findByTournamentId(tournamentId);
            List<Match> readyMatches = new ArrayList<>();

            for (Match match : matches) {
                if (match.getStatus().equals(MatchStatus.FINISHED) || match.getStatus().equals(MatchStatus.CANCELED))
                    continue;

                match.setStatus(MatchStatus.CANCELED);
                readyMatches.add(match);
            }
            matchRepository.saveAll(readyMatches);
            return new ApiResponse<>("Матчи обновлены", null, false);
        }
        catch (Exception e){
            return new ApiResponse<>(e.getMessage(), null, true);
        }
    }
}
