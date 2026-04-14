package com.deviky.Tournament_Service;

import com.deviky.Tournament_Service.bracket.bracket_core.algorithm_base.*;
import com.deviky.Tournament_Service.bracket.bracket_core.models.*;
import com.deviky.Tournament_Service.tournament_core.dto.*;
import com.deviky.Tournament_Service.tournament_core.models.*;
import com.deviky.Tournament_Service.tournament_core.repositories.TournamentRepository;
import com.deviky.Tournament_Service.tournament_core.repositories.TournamentTeamRepository;
import com.deviky.Tournament_Service.tournament_core.services.GameClientService;
import com.deviky.Tournament_Service.tournament_core.services.MatchClientService;
import com.deviky.Tournament_Service.tournament_core.services.ParticipantClientService;
import com.deviky.Tournament_Service.tournament_core.services.TournamentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class TournamentServiceTest {

    @Autowired
    private TournamentRepository tournamentRepository;
    @Autowired
    private TournamentTeamRepository tournamentTeamRepository;
    @Mock
    private BracketAlgorithmFactory bracketAlgorithmFactory;
    @Mock
    private BracketAlgorithm bracketAlgorithm;
    @Mock
    private GameClientService gameClientService;
    @Mock
    private ParticipantClientService participantClientService;
    @Mock
    private MatchClientService matchClientService;
    @Mock
    private RedisTemplate<String, String> redisTemplate;

    @InjectMocks
    private TournamentService tournamentService;

    private Tournament tournament;

    @BeforeEach
    void setup() {

        tournamentService = new TournamentService(
                tournamentRepository,
                tournamentTeamRepository,
                bracketAlgorithmFactory,
                gameClientService,
                participantClientService,
                matchClientService,
                redisTemplate
        );


        tournamentRepository.deleteAll();

        tournament = Tournament.builder()
                .organizerId(100L)
                .gameId(10)
                .name("Test Tournament")
                .description("Desc")
                .minTeams(2)
                .maxTeams(8)
                .type(TournamentType.PUBLIC)
                .status(TournamentStatus.CREATED)
                .startAt(LocalDateTime.now())
                .endAt(LocalDateTime.now().plusDays(1))
                .teams(new ArrayList<>())
                .build();
    }

    @Test
    void createTournament_Success() {
        TournamentCreateDto dto = TournamentCreateDto.builder()
                .gameId(10)
                .name("Test Tournament")
                .description("Desc")
                .minTeams(2)
                .maxTeams(8)
                .type(TournamentType.PUBLIC)
                .startAt(LocalDateTime.now())
                .endAt(LocalDateTime.now().plusDays(1))
                .build();

        // Мокируем только внешнюю проверку
        when(gameClientService.checkTournamentCreate(any()))
                .thenReturn(new ApiResponse<>("", null, false));

        ApiResponse<TournamentDto> response = tournamentService.createTournament(dto,100L);

        assertFalse(response.isError());
        assertEquals("Турнир успешно создан", response.getMessage());

        // Проверяем реально сохранённый объект
        Tournament saved = tournamentRepository.findById(response.getData().getId()).orElseThrow();
        assertEquals("Test Tournament", saved.getName());
        assertEquals(100L, saved.getOrganizerId());
    }

    @Test
    void createTournament_Fails_WhenGameServiceReturnsError() {
        TournamentCreateDto dto = TournamentCreateDto.builder()
                .gameId(10)
                .name("Test Tournament")
                .description("Desc")
                .minTeams(2)
                .maxTeams(8)
                .type(TournamentType.PUBLIC)
                .startAt(LocalDateTime.now())
                .endAt(LocalDateTime.now().plusDays(1))
                .build();

        // Мокируем внешний сервис, возвращающий ошибку
        when(gameClientService.checkTournamentCreate(any()))
                .thenReturn(new ApiResponse<>("Недопустимые параметры", null, true));

        ApiResponse<TournamentDto> response = tournamentService.createTournament(dto,100L);

        assertTrue(response.isError());
        assertEquals("Недопустимые параметры", response.getMessage());
    }

    @Test
    void createTournament_Fails_WhenEndDateBeforeStartDate() {
        TournamentCreateDto dto = TournamentCreateDto.builder()
                .gameId(10)
                .name("Test Tournament")
                .description("Desc")
                .minTeams(2)
                .maxTeams(8)
                .type(TournamentType.PUBLIC)
                // Дата окончания раньше даты старта
                .startAt(LocalDateTime.now().plusDays(1))
                .endAt(LocalDateTime.now())
                .build();

        // Мокируем внешний сервис, который считает, что все ок
        when(gameClientService.checkTournamentCreate(any()))
                .thenReturn(new ApiResponse<>("", null, false));

        ApiResponse<TournamentDto> response = tournamentService.createTournament(dto,100L);

        assertTrue(response.isError());
        assertEquals("Дата окончания турнира не может быть раньше даты старта", response.getMessage());
    }

    @Test
    void updateTournament_Success() {
        // Сохраняем турнир в репозитории
        tournament = tournamentRepository.save(tournament);

        TournamentUpdateDto updateDto = new TournamentUpdateDto(
                "New Desc",
                2,
                10,
                TournamentType.PUBLIC,
                LocalDateTime.now(),
                LocalDateTime.now().plusDays(2)
        );

        // Мокируем внешний сервис
        when(gameClientService.checkTournamentCreate(any()))
                .thenReturn(new ApiResponse<>("", null, false));

        ApiResponse<TournamentDto> response = tournamentService.updateTournament(
                tournament.getId(), 100L, updateDto
        );

        assertFalse(response.isError());
        assertEquals("Турнир успешно обновлен", response.getMessage());

        Tournament updated = tournamentRepository.findById(tournament.getId()).orElseThrow();
        assertEquals("New Desc", updated.getDescription());
        assertEquals(10, updated.getMaxTeams());
    }

    @Test
    void updateTournament_Fails_TournamentNotFound() {
        TournamentUpdateDto updateDto = new TournamentUpdateDto(
                "New Desc", 2, 10, TournamentType.PUBLIC,
                LocalDateTime.now(), LocalDateTime.now().plusDays(2)
        );

        ApiResponse<TournamentDto> response = tournamentService.updateTournament(
                999L, 100L, updateDto
        );

        assertTrue(response.isError());
        assertEquals("Такого турнира не существует", response.getMessage());
    }

    @Test
    void updateTournament_Fails_NotOrganizer() {
        tournament = tournamentRepository.save(tournament);

        TournamentUpdateDto updateDto = new TournamentUpdateDto(
                "New Desc", 2, 10, TournamentType.PUBLIC,
                LocalDateTime.now(), LocalDateTime.now().plusDays(2)
        );

        ApiResponse<TournamentDto> response = tournamentService.updateTournament(
                tournament.getId(), 999L, updateDto
        );

        assertTrue(response.isError());
        assertEquals("Только организатор может выполнять это действие", response.getMessage());
    }

    @Test
    void createFinalBracket_Success() {
        // Сохраняем турнир с нужным статусом
        tournament.setStatus(TournamentStatus.REGISTRATION_CLOSED);
        tournament = tournamentRepository.save(tournament);

        // Создаем простую сетку с одной группой и одним матчем
        BracketSlot slot1 = new BracketSlot();
        slot1.setTeamId(1L);
        BracketMatch match = new BracketMatch();
        match.setMatchId(100L);
        match.setSlots(List.of(slot1));
        BracketGroup group = new BracketGroup();
        group.setMatches(List.of(match));
        Bracket bracket = new Bracket();
        bracket.setBracketGroups(List.of(group));

        // Мокируем сервис матчей
        Match createdMatch = new Match();
        createdMatch.setId(200L);
        when(matchClientService.createMatchesByBracket(any()))
                .thenReturn(new ApiResponse<>(null, Map.of(100L, createdMatch), false));

        ApiResponse<Void> response = tournamentService.submitFinalBracket(
                tournament.getId(), 100L, bracket
        );

        assertFalse(response.isError());
        assertEquals("Турнирная сетка успешно создана", response.getMessage());

        // Проверяем, что турнир сохранил сетку и изменил статус
        Tournament saved = tournamentRepository.findById(tournament.getId()).orElseThrow();
        assertEquals(TournamentStatus.BRACKET_CREATED, saved.getStatus());
        assertNotNull(saved.getBracket());
    }

    @Test
    void createFinalBracket_Fails_TournamentNotFound() {
        Bracket bracket = new Bracket();
        bracket.setBracketGroups(Collections.emptyList());

        ApiResponse<Void> response = tournamentService.submitFinalBracket(
                999L, 100L, bracket
        );

        assertTrue(response.isError());
        assertEquals("Такого турнира не существует", response.getMessage());
    }

    @Test
    void updateBracketByMatchResult_WithRealAlgorithm_Success() {
        // Сохраняем турнир с сеткой
        Bracket existingBracket = new Bracket();
        existingBracket.setAlgorithmType("singleElim");

        tournament.setBracket(existingBracket.toJsonStr());
        tournament = tournamentRepository.save(tournament);

        MatchResult matchResult = new MatchResult();
        matchResult.setMatchId(1L);
        matchResult.setTeamsResults(Map.of(1L, MatchTeamResult.WINNER, 2L, MatchTeamResult.LOSER));

        // Мокируем фабрику алгоритмов
        when(bracketAlgorithmFactory.getAlgorithm("singleElim")).thenReturn(bracketAlgorithm);

        // Мокируем сам алгоритм
        Bracket updatedBracket = new Bracket();
        updatedBracket.setAlgorithmType("singleElim");
        when(bracketAlgorithm.update(matchResult, existingBracket)).thenReturn(updatedBracket);

        ApiResponse<Void> response = tournamentService.updateBracketByMatchResult(
                tournament.getId(), matchResult
        );

        assertFalse(response.isError());
        assertEquals("Турнирная сетка успешно обновлена по результатам матча", response.getMessage());

        Tournament saved = tournamentRepository.findById(tournament.getId()).orElseThrow();
        assertEquals(updatedBracket.toJsonStr(), saved.getBracket());
    }
    @Test
    void updateBracketByMatchResult_Fails_TournamentNotFound() {
        MatchResult matchResult = new MatchResult();

        ApiResponse<Void> response = tournamentService.updateBracketByMatchResult(
                999L, matchResult
        );

        assertTrue(response.isError());
        assertEquals("Такого турнира не существует", response.getMessage());
    }

    @Test
    void updateBracketAfterMatchCancel_Success() {
        // Сохраняем турнир с сеткой
        Bracket existingBracket = new Bracket();
        existingBracket.setAlgorithmType("singleElim");
        tournament.setBracket(existingBracket.toJsonStr());
        tournament = tournamentRepository.save(tournament);

        Long matchId = 1L;

        // Мокируем алгоритм
        when(bracketAlgorithmFactory.getAlgorithm("singleElim")).thenReturn(bracketAlgorithm);
        Bracket updatedBracket = new Bracket();
        updatedBracket.setAlgorithmType("singleElim");
        when(bracketAlgorithm.cancelMatch(matchId, existingBracket)).thenReturn(updatedBracket);

        ApiResponse<Void> response = tournamentService.updateBracketAfterMatchCancel(
                tournament.getId(), matchId
        );

        assertFalse(response.isError());
        assertEquals("Сетка обновлена после отмены матча", response.getMessage());

        Tournament saved = tournamentRepository.findById(tournament.getId()).orElseThrow();
        assertEquals(updatedBracket.toJsonStr(), saved.getBracket());
    }

    @Test
    void updateBracketAfterMatchCancel_TournamentNotFound() {
        Long invalidTournamentId = 999L;
        Long matchId = 1L;

        ApiResponse<Void> response = tournamentService.updateBracketAfterMatchCancel(
                invalidTournamentId, matchId
        );

        assertTrue(response.isError());
        assertEquals("Турнир не найден", response.getMessage());
    }

    @Test
    void getTournament_TournamentNotFound() {
        Long invalidId = 999L;
        ApiResponse<TournamentDto> response = tournamentService.getTournament(invalidId);

        assertTrue(response.isError());
        assertEquals("Такого турнира не существует", response.getMessage());
    }

    @Test
    void registerTeam_TournamentNotFound() {

        ApiResponse<Void> response = tournamentService.registerTeam(
                999L,
                1L,
                10L,
                null
        );

        assertTrue(response.isError());
        assertEquals("Такого турнира не существует", response.getMessage());
    }

    @Test
    void registerTeam_Success() {

        tournament.setStatus(TournamentStatus.REGISTRATION);
        tournament = tournamentRepository.save(tournament);

        Player captain = Player.builder()
                .id(10L)
                .isCaptain(true)
                .build();

        Team team = Team.builder()
                .id(1L)
                .gameId(tournament.getGameId())
                .players(List.of(captain))
                .build();

        when(participantClientService.getTeam(1L))
                .thenReturn(new ApiResponse<>("", team, false));

        ApiResponse<Void> response = tournamentService.registerTeam(
                tournament.getId(),
                1L,
                10L,
                null
        );

        assertEquals("Заявка отправлена", response.getMessage());

        List<TournamentTeam> teams = tournamentTeamRepository.findAll();
        assertEquals(1, teams.size());
    }

    @Test
    void registerTeam_Fails_PlayerNotCaptain() {

        tournament.setStatus(TournamentStatus.REGISTRATION);
        tournament = tournamentRepository.save(tournament);

        Player player = Player.builder()
                .id(10L)
                .isCaptain(false)
                .build();

        Team team = Team.builder()
                .id(1L)
                .gameId(tournament.getGameId())
                .players(List.of(player))
                .build();

        when(participantClientService.getTeam(1L))
                .thenReturn(new ApiResponse<>("", team, false));

        ApiResponse<Void> response = tournamentService.registerTeam(
                tournament.getId(),
                1L,
                10L,
                null
        );

        assertTrue(response.isError());
        assertEquals("Только капитан может зарегистрировать команду", response.getMessage());
    }

    // -------------------- generateInviteLink --------------------
    @Test
    void generateInviteLink_Success() {
        // Сохраняем турнир в БД
        tournament = tournamentRepository.save(tournament);

        // Мок Redis
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(ops);

        ApiResponse<String> response = tournamentService.generateInviteLink(
                tournament.getId(),
                tournament.getOrganizerId()
        );

        assertFalse(response.isError());
        assertEquals("Токен создан", response.getMessage());
        assertNotNull(response.getData());
    }

    @Test
    void generateInviteLink_Fails_NotOrganizer() {
        // Сохраняем турнир в БД
        tournament = tournamentRepository.save(tournament);
        ApiResponse<String> response = tournamentService.generateInviteLink(
                tournament.getId(), 999L
        );

        assertTrue(response.isError());
        assertEquals("Только организатор может выполнять это действие", response.getMessage());
    }

    // -------------------- leaveTeam --------------------
    @Test
    void leaveTeam_Success() {
        // Сохраняем турнир в БД
        tournament = tournamentRepository.save(tournament);

        TournamentTeam team = TournamentTeam.builder()
                .id(new TournamentTeamId(tournament.getId(), 1L))
                .tournament(tournament)
                .status(TournamentTeamStatus.REGISTERED)
                .registeredAt(LocalDateTime.now())
                .build();
        tournamentTeamRepository.save(team);


        Player captain = Player.builder().id(10L).isCaptain(true).build();
        Team teamData = Team.builder().id(1L).gameId(tournament.getGameId()).players(List.of(captain)).build();

        when(participantClientService.getTeam(1L)).thenReturn(new ApiResponse<>("", teamData, false));

        ApiResponse<String> response = tournamentService.leaveTeam(
                tournament.getId(), 1L, 10L
        );

        assertFalse(response.isError());
        assertEquals("Команда покинула турнир", response.getMessage());

        TournamentTeam updated = tournamentTeamRepository.findById(new TournamentTeamId(tournament.getId(), 1L)).orElseThrow();
        assertEquals(TournamentTeamStatus.LEAVED, updated.getStatus());
    }

    @Test
    void leaveTeam_Fails_NotCaptain() {
        // Сохраняем турнир в БД
        tournament = tournamentRepository.save(tournament);

        TournamentTeam team = TournamentTeam.builder()
                .id(new TournamentTeamId(tournament.getId(), 1L))
                .tournament(tournament)
                .status(TournamentTeamStatus.REGISTERED)
                .registeredAt(LocalDateTime.now())
                .build();
        tournamentTeamRepository.save(team);

        Player player = Player.builder().id(10L).isCaptain(false).build();
        Team teamData = Team.builder().id(1L).gameId(tournament.getGameId()).players(List.of(player)).build();

        when(participantClientService.getTeam(1L)).thenReturn(new ApiResponse<>("", teamData, false));

        ApiResponse<String> response = tournamentService.leaveTeam(
                tournament.getId(), 1L, 10L
        );

        assertTrue(response.isError());
        assertEquals("Только капитан может вывести команду из турнира", response.getMessage());
    }

    // -------------------- kickTeam --------------------
    @Test
    void kickTeam_Success() {
        // Сохраняем турнир в БД
        tournament = tournamentRepository.save(tournament);

        TournamentTeam team = TournamentTeam.builder()
                .id(new TournamentTeamId(tournament.getId(), 1L))
                .tournament(tournament)
                .status(TournamentTeamStatus.REGISTERED)
                .registeredAt(LocalDateTime.now())
                .build();
        tournamentTeamRepository.save(team);

        ApiResponse<String> response = tournamentService.kickTeam(
                tournament.getId(), 1L, tournament.getOrganizerId()
        );

        assertFalse(response.isError());
        assertEquals("Команда исключена", response.getMessage());

        TournamentTeam updated = tournamentTeamRepository.findById(new TournamentTeamId(tournament.getId(), 1L)).orElseThrow();
        assertEquals(TournamentTeamStatus.KICKED, updated.getStatus());
    }

    @Test
    void kickTeam_Fails_NotOrganizer() {
        // Сохраняем турнир в БД
        tournament = tournamentRepository.save(tournament);

        TournamentTeam team = TournamentTeam.builder()
                .id(new TournamentTeamId(tournament.getId(), 1L))
                .tournament(tournament)
                .status(TournamentTeamStatus.REGISTERED)
                .registeredAt(LocalDateTime.now())
                .build();
        tournamentTeamRepository.save(team);


        ApiResponse<String> response = tournamentService.kickTeam(
                tournament.getId(), 1L, 999L
        );

        assertTrue(response.isError());
        assertEquals("Только организатор может исключать команды", response.getMessage());
    }

    // -------------------- handleRequestTeamTournament --------------------
    @Test
    void handleRequestTeamTournament_Success_Approve() {
        // Сохраняем турнир в БД
        tournament = tournamentRepository.save(tournament);

        TournamentTeam team = TournamentTeam.builder()
                .id(new TournamentTeamId(tournament.getId(), 1L))
                .tournament(tournament)
                .status(TournamentTeamStatus.WAITING_APPROVE)
                .registeredAt(LocalDateTime.now())
                .build();
        tournamentTeamRepository.save(team);

        ApiResponse<String> response = tournamentService.handleRequestTeamTournament(
                tournament.getId(), 1L, tournament.getOrganizerId(), true
        );

        assertFalse(response.isError());
        assertEquals("Статус заявки обновлен", response.getMessage());

        TournamentTeam updated = tournamentTeamRepository.findById(new TournamentTeamId(tournament.getId(), 1L)).orElseThrow();
        assertEquals(TournamentTeamStatus.REGISTERED, updated.getStatus());
    }

    @Test
    void handleRequestTeamTournament_Fails_NotWaiting() {
        // Сохраняем турнир в БД
        tournament = tournamentRepository.save(tournament);

        TournamentTeam team = TournamentTeam.builder()
                .id(new TournamentTeamId(tournament.getId(), 1L))
                .tournament(tournament)
                .status(TournamentTeamStatus.REGISTERED)
                .registeredAt(LocalDateTime.now())
                .build();
        tournamentTeamRepository.save(team);


        ApiResponse<String> response = tournamentService.handleRequestTeamTournament(
                tournament.getId(), 1L, tournament.getOrganizerId(), true
        );

        assertTrue(response.isError());
        assertEquals("Команда не ожидает подтверждения", response.getMessage());
    }

}