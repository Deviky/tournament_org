package com.deviky.Tournament_Service.tournament_core.services;

import com.deviky.Tournament_Service.bracket.bracket_core.algorithm_base.AlgorithmParams;
import com.deviky.Tournament_Service.bracket.bracket_core.algorithm_base.BracketAlgorithm;
import com.deviky.Tournament_Service.bracket.bracket_core.algorithm_base.BracketAlgorithmFactory;
import com.deviky.Tournament_Service.bracket.bracket_core.models.*;
import com.deviky.Tournament_Service.tournament_core.dto.*;
import com.deviky.Tournament_Service.tournament_core.models.*;
import com.deviky.Tournament_Service.tournament_core.repositories.TournamentRepository;
import com.deviky.Tournament_Service.tournament_core.repositories.TournamentTeamRepository;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TournamentService {

    private final TournamentRepository tournamentRepository;
    private final TournamentTeamRepository tournamentTeamsRepository;
    private final BracketAlgorithmFactory bracketAlgorithmFactory;
    private final GameClientService gameClientService;
    private final ParticipantClientService participantClientService;
    private final MatchClientService matchClientService;
    private final RedisTemplate<String, String> redisTemplate;

    public ApiResponse<Map<String, ObjectNode>> getAlgorithms(Integer gameId){
        try{
            ApiResponse<List<String>> algorithmsNamesResponse = gameClientService.getBracketAlgorithms(gameId);
            if (algorithmsNamesResponse.isError())
                return new ApiResponse<>(algorithmsNamesResponse.getMessage(), null, true);
            List<String> algorithmsNames = algorithmsNamesResponse.getData();

            Map<String, BracketAlgorithm> algorithms = bracketAlgorithmFactory.getAllAlgorithms();
            Map<String, ObjectNode> algorithmsWithConfigs = new HashMap<>();

            for (String algorithmName : algorithmsNames) {
                BracketAlgorithm algorithm = algorithms.get(algorithmName);
                if (algorithm != null) {
                    try {
                        AlgorithmParams paramsInstance = algorithm.getAlgorithmParamsClass()
                                .getDeclaredConstructor()
                                .newInstance();

                        ObjectNode jsonView = paramsInstance.getJsonView();
                        algorithmsWithConfigs.put(algorithmName, jsonView);

                    } catch (Exception e) {
                        return new ApiResponse<>("Ошибка при создании параметров турнирной сетки для заполнения: " + algorithmName, null, true);
                    }
                }
            }
            return new ApiResponse<>("",algorithmsWithConfigs,false);

        }
        catch (Exception e){
            return new ApiResponse<>(e.getMessage(), null, true);
        }
    }


    public ApiResponse<Bracket> generateBracket(Long tournamentId, Long organizerId, TournamentGenerateBracketDto dto){

        Tournament tournament = tournamentRepository.findById(tournamentId).orElse(null);

        if (tournament == null){
            return new ApiResponse<>("Турнир не найден", null, true);
        }

        if (!Objects.equals(tournament.getOrganizerId(), organizerId)) {
            return new ApiResponse<>("Только организатор может выполнять это действие", null, true);
        }

        if (!(tournament.getStatus().equals(TournamentStatus.REGISTRATION_CLOSED)))
            return new ApiResponse<>("Невозможно создать турнирную сетку с текущим статусом турнира", null, true);

        ApiResponse<List<String>> algorithmsNamesResponse = gameClientService.getBracketAlgorithms(tournament.getGameId());

        if (algorithmsNamesResponse.isError())
            return new ApiResponse<>(algorithmsNamesResponse.getMessage(), null, true);

        List<String> algorithmsNames = algorithmsNamesResponse.getData();

        if (!algorithmsNames.contains(dto.getAlgorithmName())){
            return new ApiResponse<>("Игра не поддерживает данный алгоритм формирования турнирной сетки.", null, true);
        }

        BracketAlgorithm bracketAlgorithm = bracketAlgorithmFactory.getAlgorithm(dto.getAlgorithmName());

        List<TournamentTeam> tournamentTeams = tournament.getTeams();
        List<Long> teamIds = tournamentTeams.stream()
                .filter(team -> team.getStatus() == TournamentTeamStatus.REGISTERED)
                .map(TournamentTeam::getId)
                .map(TournamentTeamId::getTeamId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();


        Bracket bracket = bracketAlgorithm.generate(teamIds, dto.getAlgorithmParams());

        tournament.setBracket(bracket.toJsonStr());
        tournamentRepository.save(tournament);

        return new ApiResponse<>("", bracket,false);

    }

    @Transactional
    public ApiResponse<TournamentDto> createTournament(TournamentCreateDto tournamentCreateDto, Long organizerId){
        try {
            // Проверка, что дата окончания не раньше даты старта
            if (tournamentCreateDto.getEndAt() != null && tournamentCreateDto.getEndAt().isBefore(tournamentCreateDto.getStartAt())) {
                return new ApiResponse<>("Дата окончания турнира не может быть раньше даты старта", null, true);
            }

            TournamentDto tournamentDto = TournamentDto.builder()
                    .organizerId(organizerId)
                    .gameId(tournamentCreateDto.getGameId())
                    .name(tournamentCreateDto.getName())
                    .description(tournamentCreateDto.getDescription())
                    .minTeams(tournamentCreateDto.getMinTeams())
                    .maxTeams(tournamentCreateDto.getMaxTeams())
                    .type(tournamentCreateDto.getType())
                    .status(TournamentStatus.CREATED)
                    .startAt(tournamentCreateDto.getStartAt())
                    .endAt(tournamentCreateDto.getEndAt())
                    .build();

            ApiResponse<Void> checkResult = gameClientService.checkTournamentCreate(tournamentDto);

            if (checkResult.isError())
                return new ApiResponse<>(checkResult.getMessage(), null, true);
            else{
                Tournament tournament = Tournament.builder()
                        .organizerId(organizerId)
                        .gameId(tournamentCreateDto.getGameId())
                        .name(tournamentCreateDto.getName())
                        .description(tournamentCreateDto.getDescription())
                        .minTeams(tournamentCreateDto.getMinTeams())
                        .maxTeams(tournamentCreateDto.getMaxTeams())
                        .type(tournamentCreateDto.getType())
                        .status(TournamentStatus.CREATED)
                        .startAt(tournamentCreateDto.getStartAt())
                        .endAt(tournamentCreateDto.getEndAt())
                        .build();
                Tournament tournamentSaved = tournamentRepository.save(tournament);
                tournamentDto.setId(tournamentSaved.getId());
                return new ApiResponse<>("Турнир успешно создан", tournamentDto, false);
            }
        } catch (Exception e){
            return new ApiResponse<>("Ошибка при создании турнира: " + e.getMessage(), null, true);
        }
    }

    @Transactional
    public ApiResponse<TournamentDto> updateTournament(Long tournamentId, Long organizerId, TournamentUpdateDto tournamentUpdateDto) {
        try {
            Tournament tournament = tournamentRepository.findById(tournamentId).orElse(null);

            if (tournament == null) {
                return new ApiResponse<>("Такого турнира не существует", null, true);
            }

            if (!Objects.equals(tournament.getOrganizerId(), organizerId)) {
                return new ApiResponse<>("Только организатор может выполнять это действие", null, true);
            }

            if (!(tournament.getStatus().equals(TournamentStatus.CREATED) || tournament.getStatus().equals(TournamentStatus.REGISTRATION)))
                return new ApiResponse<>("Невозможно обновить турнир с текущим статусом", null, true);

            tournament.setDescription(tournamentUpdateDto.getDescription());
            tournament.setMinTeams(tournamentUpdateDto.getMinTeams());
            tournament.setMaxTeams(tournamentUpdateDto.getMaxTeams());
            tournament.setType(tournamentUpdateDto.getType());
            tournament.setStartAt(tournamentUpdateDto.getStartAt());
            tournament.setEndAt(tournamentUpdateDto.getEndAt());

            TournamentDto tournamentDtoForCheck = TournamentDto.builder()
                    .id(tournament.getId())
                    .organizerId(tournament.getOrganizerId())
                    .gameId(tournament.getGameId())
                    .name(tournament.getName())
                    .description(tournament.getDescription())
                    .minTeams(tournament.getMinTeams())
                    .maxTeams(tournament.getMaxTeams())
                    .type(tournament.getType())
                    .startAt(tournament.getStartAt())
                    .endAt(tournament.getEndAt())
                    .build();

            ApiResponse<Void> checkResult = gameClientService.checkTournamentCreate(tournamentDtoForCheck);

            if (checkResult.isError()) {
                return new ApiResponse<>(checkResult.getMessage(), null, true);
            }

            tournamentRepository.save(tournament);

            return new ApiResponse<>("Турнир успешно обновлен", tournamentDtoForCheck, false);

        } catch (Exception e) {
            return new ApiResponse<>("Ошибка при обновлении турнира: " + e.getMessage(), null, true);
        }
    }


    @Transactional
    public ApiResponse<Void> submitFinalBracket(Long tournamentId, Long organizerId, Bracket bracket) {
        try {

            Tournament tournament = tournamentRepository.findById(tournamentId).orElse(null);

            if (tournament == null)
                return new ApiResponse<>("Такого турнира не существует", null, true);

            if (!Objects.equals(tournament.getOrganizerId(), organizerId))
                return new ApiResponse<>("Только организатор может выполнять это действие", null, true);

            if (tournament.getStatus() != TournamentStatus.REGISTRATION_CLOSED)
                return new ApiResponse<>("Невозможно создать турнирную сетку", null, true);

            if (tournament.getBracket() != null)
                return new ApiResponse<>("Сетка уже создана", null, true);


            Map<Long, CreateMatchDto> matchesToCreate = new HashMap<>();

            for (BracketGroup group : bracket.getBracketGroups()) {

                for (BracketMatch match : group.getMatches()) {

                    CreateMatchDto dto = new CreateMatchDto();
                    dto.setTournamentId(tournamentId);

                    List<Long> teamIds = match.getSlots()
                            .stream()
                            .map(BracketSlot::getTeamId)
                            .filter(Objects::nonNull)
                            .toList();

                    dto.setTeamIds(teamIds);

                    matchesToCreate.put(match.getMatchId(), dto);
                }
            }


            ApiResponse<Map<Long, Match>> matchesResponse =
                    matchClientService.createMatchesByBracket(matchesToCreate);

            if (matchesResponse.isError())
                return new ApiResponse<>(matchesResponse.getMessage(), null, true);


            Map<Long, Long> idMapping = matchesResponse.getData()
                    .entrySet()
                    .stream()
                    .collect(Collectors.toMap(
                            Map.Entry::getKey,
                            e -> e.getValue().getId()
                    ));


            for (BracketGroup group : bracket.getBracketGroups()) {

                for (BracketMatch match : group.getMatches()) {

                    Long tempId = match.getMatchId();

                    if (idMapping.containsKey(tempId)) {
                        match.setMatchId(idMapping.get(tempId));
                    }
                }
            }


            for (BracketGroup group : bracket.getBracketGroups()) {

                for (BracketMatch match : group.getMatches()) {

                    for (BracketSlot slot : match.getSlots()) {

                        Long refMatchId = slot.getRefMatchId();

                        if (refMatchId != null && idMapping.containsKey(refMatchId)) {
                            slot.setRefMatchId(idMapping.get(refMatchId));
                        }
                    }
                }
            }

            tournament.setBracket(bracket.toJsonStr());
            tournament.setStatus(TournamentStatus.BRACKET_CREATED);

            tournamentRepository.save(tournament);

            return new ApiResponse<>("Турнирная сетка успешно создана", null, false);

        } catch (Exception e) {
            return new ApiResponse<>("Ошибка при создании сетки: " + e.getMessage(), null, true);
        }
    }

    public ApiResponse<Void> updateBracketByMatchResult(Long tournamentId, MatchResult matchResult){
        try {
            Tournament tournament = tournamentRepository.findById(tournamentId).orElse(null);

            if (tournament == null) {
                return new ApiResponse<>("Такого турнира не существует", null, true);
            }

            Bracket curBracket = new Bracket(tournament.getBracket());

            BracketAlgorithm bracketAlgorithm = bracketAlgorithmFactory.getAlgorithm(curBracket.getAlgorithmType());

            Bracket newBracket = bracketAlgorithm.update(matchResult, curBracket);

            tournament.setBracket(newBracket.toJsonStr());
            tournamentRepository.save(tournament);

            return new ApiResponse<>("Турнирная сетка успешно обновлена по результатам матча", null, false);

        } catch (Exception e) {
            return new ApiResponse<>("Ошибка при обновлении сетки по результатам матча: " + e.getMessage(), null, true);
        }
    }

    @Transactional
    public ApiResponse<Void> updateBracketAfterMatchCancel(Long tournamentId, Long matchId) {

        try {

            Tournament tournament = tournamentRepository.findById(tournamentId).orElse(null);

            if (tournament == null) {
                return new ApiResponse<>("Турнир не найден", null, true);
            }

            Bracket bracket = new Bracket(tournament.getBracket());

            BracketAlgorithm algorithm =
                    bracketAlgorithmFactory.getAlgorithm(bracket.getAlgorithmType());

            Bracket newBracket = algorithm.cancelMatch(matchId, bracket);

            tournament.setBracket(newBracket.toJsonStr());
            tournamentRepository.save(tournament);

            return new ApiResponse<>("Сетка обновлена после отмены матча", null, false);

        }
        catch (Exception e) {
            return new ApiResponse<>("Ошибка обновления сетки: " + e.getMessage(), null, true);
        }
    }

    public ApiResponse<TournamentDto> getTournament(Long tournamentId) {

        try {

            Tournament tournament = tournamentRepository.findById(tournamentId).orElse(null);

            if (tournament == null) {
                return new ApiResponse<>("Такого турнира не существует", null, true);
            }

            List<Long> teamIds = tournament.getTeams()
                    .stream()
                    .map(tt -> tt.getId().getTeamId())
                    .toList();

            ApiResponse<List<Team>> teamsResponse =
                    participantClientService.getTeams(teamIds);

            if (teamsResponse.isError())
                return new ApiResponse<>(teamsResponse.getMessage(), null, true);


            ApiResponse<List<Match>> matchesResponse =
                    matchClientService.getMatchesByTournament(tournamentId);

            if (matchesResponse.isError())
                return new ApiResponse<>(matchesResponse.getMessage(), null, true);


            ApiResponse<Organization> organizationResponse =
                    participantClientService.getOrganization(tournament.getOrganizerId());

            if (organizationResponse.isError())
                return new ApiResponse<>(organizationResponse.getMessage(), null, true);

            ApiResponse<Game> gameResponse =
                    gameClientService.getGame(tournament.getGameId());

            if (gameResponse.isError())
                return new ApiResponse<>(gameResponse.getMessage(), null, true);


            Bracket bracket = null;

            if (tournament.getBracket() != null) {
                bracket = new Bracket(tournament.getBracket());
            }


            TournamentDto dto = TournamentDto.builder()
                    .id(tournament.getId())
                    .organizerId(tournament.getOrganizerId())
                    .gameId(tournament.getGameId())
                    .game(gameResponse.getData())
                    .name(tournament.getName())
                    .description(tournament.getDescription())
                    .minTeams(tournament.getMinTeams())
                    .maxTeams(tournament.getMaxTeams())
                    .type(tournament.getType())
                    .status(tournament.getStatus())
                    .startAt(tournament.getStartAt())
                    .endAt(tournament.getEndAt())
                    .bracket(bracket)
                    .organization(organizationResponse.getData())
                    .teams(teamsResponse.getData())
                    .matches(matchesResponse.getData())
                    .build();

            return new ApiResponse<>("", dto, false);

        }
        catch (Exception e) {
            return new ApiResponse<>("Ошибка при получении турнира: " + e.getMessage(), null, true);
        }
    }

    public ApiResponse<List<TournamentDto>> getTournaments(){
        try {
            List<Tournament> tournaments = tournamentRepository.findAll();

            List<TournamentDto> tournamentDtos = tournaments.stream()
                    .map(tournament -> TournamentDto.builder()
                            .id(tournament.getId())
                            .organizerId(tournament.getOrganizerId())
                            .gameId(tournament.getGameId())
                            .name(tournament.getName())
                            .description(tournament.getDescription())
                            .minTeams(tournament.getMinTeams())
                            .maxTeams(tournament.getMaxTeams())
                            .type(tournament.getType())
                            .status(tournament.getStatus())
                            .startAt(tournament.getStartAt())
                            .endAt(tournament.getEndAt())
                            .build())
                    .collect(Collectors.toList());
            return new ApiResponse<>("", tournamentDtos, false);
        }
        catch (Exception e){
            return new ApiResponse<>("Ошибка при получении турниров " + e.getMessage(), null, true);
        }
    }

    public ApiResponse<List<TournamentDto>> getTournamentsByGame(Integer gameId){
        try {
            List<Tournament> tournaments = tournamentRepository.findByGameId(gameId);

            List<TournamentDto> tournamentDtos = tournaments.stream()
                    .map(tournament -> TournamentDto.builder()
                            .id(tournament.getId())
                            .organizerId(tournament.getOrganizerId())
                            .gameId(tournament.getGameId())
                            .name(tournament.getName())
                            .description(tournament.getDescription())
                            .minTeams(tournament.getMinTeams())
                            .maxTeams(tournament.getMaxTeams())
                            .type(tournament.getType())
                            .status(tournament.getStatus())
                            .startAt(tournament.getStartAt())
                            .endAt(tournament.getEndAt())
                            .build())
                    .collect(Collectors.toList());
            return new ApiResponse<>("", tournamentDtos, false);
        }
        catch (Exception e){
            return new ApiResponse<>("Ошибка при получении турниров по игре " + e.getMessage(), null, true);
        }
    }



    public ApiResponse<List<TournamentDto>> getTournamentsByGames(List<Integer> gameIds){
        try {
            List<Tournament> tournaments = tournamentRepository.findByGameIdIn(gameIds);

            List<TournamentDto> tournamentDtos = tournaments.stream()
                    .map(tournament -> TournamentDto.builder()
                            .id(tournament.getId())
                            .organizerId(tournament.getOrganizerId())
                            .gameId(tournament.getGameId())
                            .name(tournament.getName())
                            .description(tournament.getDescription())
                            .minTeams(tournament.getMinTeams())
                            .maxTeams(tournament.getMaxTeams())
                            .type(tournament.getType())
                            .status(tournament.getStatus())
                            .startAt(tournament.getStartAt())
                            .endAt(tournament.getEndAt())
                            .build())
                    .collect(Collectors.toList());
            return new ApiResponse<>("", tournamentDtos, false);
        }
        catch (Exception e){
            return new ApiResponse<>("Ошибка при получении турниров по играм " + e.getMessage(), null, true);
        }
    }

    @Transactional
    public ApiResponse<Void> registerTeam(Long tournamentId,
                                          Long teamId,
                                          Long playerId,
                                          String inviteToken) {
        try {
            Tournament tournament = tournamentRepository.findById(tournamentId).orElse(null);
            if (tournament == null) {
                return new ApiResponse<>("Такого турнира не существует", null, true);
            }

            if (!tournament.getStatus().equals(TournamentStatus.REGISTRATION)) {
                return new ApiResponse<>("Нельзя зарегистрироваться на турнир с текущим статусом", null, true);
            }

            // Проверка существующей регистрации
            for (TournamentTeam tournamentTeam : tournament.getTeams()) {
                if (Objects.equals(tournamentTeam.getId().getTeamId(), teamId)) {

                    switch (tournamentTeam.getStatus()) {
                        case REGISTERED ->
                        { return new ApiResponse<>("Команда уже зарегистрирована", null, true); }
                        case WAITING_APPROVE ->
                        { return new ApiResponse<>("Заявка уже отправлена", null, true); }
                        case KICKED ->
                        { return new ApiResponse<>("Команда не может участвовать в турнире", null, true); }
                    }
                }
            }

            ApiResponse<Team> response = participantClientService.getTeam(teamId);
            if (response.isError()) {
                return new ApiResponse<>(response.getMessage(), null, true);
            }

            Team team = response.getData();

            if (!Objects.equals(team.getGameId(), tournament.getGameId())) {
                return new ApiResponse<>("Команда не относится к дисциплине турнира", null, true);
            }

            boolean isCaptain = false;

            for (Player player : team.getPlayers()) {
                if (Objects.equals(player.getId(), playerId)) {
                    if (!player.isCaptain()) {
                        return new ApiResponse<>("Только капитан может зарегистрировать команду", null, true);
                    }
                    isCaptain = true;
                }
            }

            if (!isCaptain) {
                return new ApiResponse<>("Вы не состоите в данной команде", null, true);
            }

            // 🔐 Логика токена
            if (inviteToken != null && !inviteToken.isBlank()) {

                String redisKey = "tournament:invite:" + inviteToken;
                String redisTournamentId = redisTemplate.opsForValue().get(redisKey);

                if (redisTournamentId == null) {
                    return new ApiResponse<>("Неверный или просроченный токен приглашения", null, true);
                }

                if (!redisTournamentId.equals(String.valueOf(tournamentId))) {
                    return new ApiResponse<>("Токен не относится к данному турниру", null, true);
                }

            } else {
                if (tournament.getType().equals(TournamentType.PRIVATE)) {
                    return new ApiResponse<>("Для участия в приватном турнире требуется токен приглашения", null, true);
                }
            }

            TournamentTeam tournamentTeam = TournamentTeam.builder()
                    .id(TournamentTeamId.builder()
                            .tournamentId(tournamentId)
                            .teamId(teamId)
                            .build())
                    .tournament(tournament)
                    .registeredAt(LocalDateTime.now())
                    .status(TournamentTeamStatus.WAITING_APPROVE)
                    .build();

            tournamentTeamsRepository.save(tournamentTeam);

            return new ApiResponse<>("Заявка отправлена", null, false);

        } catch (Exception e) {
            return new ApiResponse<>("Ошибка при регистрации: " + e.getMessage(), null, true);
        }
    }

    public ApiResponse<String> generateInviteLink(Long tournamentId, Long organizerId) {
        try {
            Tournament tournament = tournamentRepository.findById(tournamentId).orElse(null);

            if (tournament == null) {
                return new ApiResponse<>("Турнир не найден", null, true);
            }

            if (!Objects.equals(tournament.getOrganizerId(), organizerId)) {
                return new ApiResponse<>("Только организатор может выполнять это действие", null, true);
            }

            String token = UUID.randomUUID().toString();

            String redisKey = "tournament:invite:" + token;

            // TTL 1 день
            redisTemplate.opsForValue()
                    .set(redisKey, String.valueOf(tournamentId), Duration.ofDays(1));

            return new ApiResponse<>("Токен создан", token, false);

        } catch (Exception e) {
            return new ApiResponse<>("Ошибка генерации токена: " + e.getMessage(), null, true);
        }
    }

    @Transactional
    public ApiResponse<String> leaveTeam(Long tournamentId,
                                         Long teamId,
                                         Long playerId) {
        try {

            Tournament tournament = tournamentRepository
                    .findById(tournamentId)
                    .orElse(null);

            if (tournament == null) {
                return new ApiResponse<>("Турнир не найден", null, true);
            }

            TournamentTeamId id =
                    new TournamentTeamId(tournamentId, teamId);

            TournamentTeam tournamentTeam =
                    tournamentTeamsRepository.findById(id).orElse(null);

            if (tournamentTeam == null) {
                return new ApiResponse<>("Команда не участвует в турнире", null, true);
            }

            // 🚫 Нельзя выйти после старта турнира
            if (tournament.getStatus() != TournamentStatus.REGISTRATION) {
                return new ApiResponse<>("Нельзя выйти из турнира не во время регистрации", null, true);
            }

            // 🔥 Получаем команду через participant-service
            ApiResponse<Team> response =
                    participantClientService.getTeam(teamId);

            if (response.isError()) {
                return new ApiResponse<>(response.getMessage(), null, true);
            }

            Team team = response.getData();

            boolean isCaptain = false;

            for (Player player : team.getPlayers()) {

                if (Objects.equals(player.getId(), playerId)) {

                    if (!player.isCaptain()) {
                        return new ApiResponse<>(
                                "Только капитан может вывести команду из турнира",
                                null,
                                true
                        );
                    }

                    isCaptain = true;
                }
            }

            if (!isCaptain) {
                return new ApiResponse<>(
                        "Вы не состоите в данной команде",
                        null,
                        true
                );
            }

            // ✅ Меняем статус
            tournamentTeam.setStatus(TournamentTeamStatus.LEAVED);
            tournamentTeamsRepository.save(tournamentTeam);

            return new ApiResponse<>("Команда покинула турнир", null, false);

        } catch (Exception e) {
            return new ApiResponse<>("Ошибка выхода: " + e.getMessage(), null, true);
        }
    }

    @Transactional
    public ApiResponse<String> kickTeam(Long tournamentId,
                                        Long teamId,
                                        Long organizerId) {
        try {
            Tournament tournament = tournamentRepository.findById(tournamentId).orElse(null);

            if (tournament == null) {
                return new ApiResponse<>("Турнир не найден", null, true);
            }

            if (!Objects.equals(tournament.getOrganizerId(), organizerId)) {
                return new ApiResponse<>("Только организатор может исключать команды", null, true);
            }

            TournamentTeamId id = new TournamentTeamId(tournamentId, teamId);

            TournamentTeam tournamentTeam =
                    tournamentTeamsRepository.findById(id).orElse(null);

            if (tournamentTeam == null) {
                return new ApiResponse<>("Команда не участвует в турнире", null, true);
            }

            tournamentTeam.setStatus(TournamentTeamStatus.KICKED);
            tournamentTeamsRepository.save(tournamentTeam);

            return new ApiResponse<>("Команда исключена", null, false);

        } catch (Exception e) {
            return new ApiResponse<>("Ошибка при исключении: " + e.getMessage(), null, true);
        }
    }

    @Transactional
    public ApiResponse<String> handleRequestTeamTournament(Long tournamentId,
                                                           Long teamId,
                                                           Long organizerId,
                                                           boolean approve) {
        try {
            Tournament tournament = tournamentRepository.findById(tournamentId).orElse(null);

            if (tournament == null) {
                return new ApiResponse<>("Турнир не найден", null, true);
            }

            if (!Objects.equals(tournament.getOrganizerId(), organizerId)) {
                return new ApiResponse<>("Только организатор может обрабатывать заявки", null, true);
            }

            TournamentTeamId id = new TournamentTeamId(tournamentId, teamId);

            TournamentTeam tournamentTeam = tournament.getTeams().stream()
                    .filter(t -> Objects.equals(t.getId().getTeamId(), teamId))
                    .findFirst()
                    .orElse(null);

            if (tournamentTeam == null) {
                return new ApiResponse<>("Заявка не найдена", null, true);
            }

            if (!tournamentTeam.getStatus().equals(TournamentTeamStatus.WAITING_APPROVE)) {
                return new ApiResponse<>("Команда не ожидает подтверждения", null, true);
            }

            if (approve) {
                tournamentTeam.setStatus(TournamentTeamStatus.REGISTERED);
            } else {
                tournamentTeam.setStatus(TournamentTeamStatus.KICKED);
            }

            tournamentTeamsRepository.save(tournamentTeam);

            return new ApiResponse<>("Статус заявки обновлен", null, false);

        } catch (Exception e) {
            return new ApiResponse<>("Ошибка обработки заявки: " + e.getMessage(), null, true);
        }
    }

    @Transactional
    public ApiResponse<String> startRegistrationTournament(Long tournamentId, Long organizerId) {

        Tournament tournament = tournamentRepository.findById(tournamentId).orElse(null);

        if (tournament == null)
            return new ApiResponse<>("Турнир не найден", null, true);

        if (!Objects.equals(tournament.getOrganizerId(), organizerId)) {
            return new ApiResponse<>("Только организатор может выполнять это действие", null, true);
        }

        if (tournament.getStatus() != TournamentStatus.CREATED)
            return new ApiResponse<>("Турнир нельзя запустить в текущем статусе", null, true);


        TournamentDto tournamentDto = TournamentDto.builder()
                .id(tournament.getId())
                .organizerId(tournament.getOrganizerId())
                .gameId(tournament.getGameId())
                .build();

        tournament.setStatus(TournamentStatus.REGISTRATION);
        tournamentRepository.save(tournament);

        return new ApiResponse<>("Началась регистрация на турнир", null, false);
    }

    @Transactional
    public ApiResponse<String> startTournament(Long tournamentId, Long organizerId) {

        Tournament tournament = tournamentRepository.findById(tournamentId).orElse(null);

        if (tournament == null)
            return new ApiResponse<>("Турнир не найден", null, true);

        if (!Objects.equals(tournament.getOrganizerId(), organizerId)) {
            return new ApiResponse<>("Только организатор может выполнять это действие", null, true);
        }

        if (tournament.getStatus() != TournamentStatus.BRACKET_CREATED)
            return new ApiResponse<>("Турнир нельзя запустить в текущем статусе", null, true);

        List<Team> teams = new ArrayList<>();
        for (TournamentTeam tt : tournament.getTeams()) {
            ApiResponse<Team> teamResponse = participantClientService.getTeam(tt.getId().getTeamId());
            if (teamResponse.isError()) {
                return new ApiResponse<>("Не удалось получить команду с id " + tt.getId().getTeamId()
                        + ": " + teamResponse.getMessage(), null, true);
            }
            teams.add(teamResponse.getData());
        }

        TournamentDto tournamentDto = TournamentDto.builder()
                .id(tournament.getId())
                .organizerId(tournament.getOrganizerId())
                .gameId(tournament.getGameId())
                .teams(teams)
                .build();

        ApiResponse<Void> checkResult = gameClientService.checkTournamentStart(tournamentDto);

        if (checkResult.isError())
            return new ApiResponse<>(checkResult.getMessage(), null, true);

        tournament.setStatus(TournamentStatus.RUNNING);
        tournamentRepository.save(tournament);

        return new ApiResponse<>("Турнир запущен", null, false);
    }

    @Transactional
    public ApiResponse<String> endTournament(Long tournamentId, Long organizerId) {

        Tournament tournament = tournamentRepository.findById(tournamentId).orElse(null);

        if (tournament == null)
            return new ApiResponse<>("Турнир не найден", null, true);

        if (!Objects.equals(tournament.getOrganizerId(), organizerId)) {
            return new ApiResponse<>("Только организатор может выполнять это действие", null, true);
        }

        if (tournament.getStatus() != TournamentStatus.RUNNING)
            return new ApiResponse<>("Турнир не запущен", null, true);

        tournament.setStatus(TournamentStatus.FINISHED);
        tournamentRepository.save(tournament);

        return new ApiResponse<>("Турнир завершён", null, false);
    }

    @Transactional
    public ApiResponse<String> banTournament(Long tournamentId) {

        Tournament tournament = tournamentRepository.findById(tournamentId).orElse(null);

        if (tournament == null)
            return new ApiResponse<>("Турнир не найден", null, true);

        if (tournament.getStatus() == TournamentStatus.FINISHED)
            return new ApiResponse<>("Нельзя заблокировать завершённый турнир", null, true);

        tournament.setStatus(TournamentStatus.BANNED);
        tournamentRepository.save(tournament);

        ApiResponse<Void> cancelResponse = matchClientService.cancelMatchesByTournament(tournamentId);
        if (cancelResponse.isError()) {
            // Логируем ошибку или кидаем исключение
            throw new RuntimeException("Не удалось отменить матчи турнира: " + cancelResponse.getMessage());
        }
        return new ApiResponse<>("Турнир заблокирован", null, false);
    }

    @Transactional
    public ApiResponse<String> cancelTournament(Long tournamentId, Long organizerId) {

        try {

            Tournament tournament = tournamentRepository.findById(tournamentId).orElse(null);

            if (tournament == null)
                return new ApiResponse<>("Турнир не найден", null, true);

            if (!Objects.equals(tournament.getOrganizerId(), organizerId))
                return new ApiResponse<>("Только организатор может отменить турнир", null, true);

            if (tournament.getStatus() == TournamentStatus.FINISHED)
                return new ApiResponse<>("Нельзя отменить завершённый турнир", null, true);

            tournament.setStatus(TournamentStatus.CANCEL);

            tournamentRepository.save(tournament);

            ApiResponse<Void> cancelResponse = matchClientService.cancelMatchesByTournament(tournamentId);
            if (cancelResponse.isError()) {
                // Логируем ошибку или кидаем исключение
                throw new RuntimeException("Не удалось отменить матчи турнира: " + cancelResponse.getMessage());
            }
            return new ApiResponse<>("Турнир отменён", null, false);

        } catch (Exception e) {
            return new ApiResponse<>("Ошибка отмены турнира: " + e.getMessage(), null, true);
        }
    }

    public ApiResponse<Void> checkTournamentMatchCreate(Long tournamentId, Long organizerId){
        Tournament tournament = tournamentRepository.findById(tournamentId).orElse(null);

        if (tournament == null)
            return new ApiResponse<>("Турнир не найден", null, true);

        if (!tournament.getOrganizerId().equals(organizerId))
            return new ApiResponse<>("Вы не являетесь организатором данного турнира", null, true);

        if (!(tournament.getStatus() == TournamentStatus.RUNNING || tournament.getStatus() == TournamentStatus.REGISTRATION_CLOSED))
            return new ApiResponse<>("Нельзя создать матч для данного турнира с текущим статусом", null, true);

        return new ApiResponse<>("", null, false);
    }
}
