package com.deviky.Participant_Service.services;

import com.deviky.Participant_Service.dto.*;
import com.deviky.Participant_Service.models.*;
import com.deviky.Participant_Service.repositories.PlayerRepository;
import com.deviky.Participant_Service.repositories.TeamPlayerRepository;
import com.deviky.Participant_Service.repositories.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final PlayerRepository playerRepository;
    private final TeamPlayerRepository teamPlayerRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final GameClientService gameClientService;

    private final SecureRandom random = new SecureRandom();

    // ------------------ Создание команды ------------------
    public ApiResponse<TeamDto> createTeam(CreateTeamRequest request, Long selfPlayerId) {
        try {
            Player creator = playerRepository.findById(selfPlayerId)
                    .orElseThrow(() -> new Exception("Игрок не найден"));

            ApiResponse<Game> gameResponse = gameClientService.getGame(request.getGameId());
            if (gameResponse.isError()){
                return new ApiResponse<>(gameResponse.getMessage(), null, true);
            }

            Team team = new Team();
            team.setName(request.getName());
            team.setGameId(request.getGameId());
            team.setStatus(TeamStatus.ACTIVE);
            team.setType(request.getType());
            team = teamRepository.save(team);

            TeamPlayer tp = new TeamPlayer();
            tp.setId(new TeamPlayerId(creator.getId(), team.getId()));
            tp.setPlayer(creator);
            tp.setTeam(team);
            tp.setCaptain(true);
            tp.setStatus(TeamPlayerStatus.ACTIVE);
            teamPlayerRepository.save(tp);

            return new ApiResponse<>("Команда создана", getTeamWithPlayers(team.getId()).getData(), false);
        } catch (Exception ex) {
            return new ApiResponse<>("Ошибка при создании команды: " + ex.getMessage(), null, true);
        }
    }

    // ------------------ Добавление игрока в команду ------------------
    @Transactional
    public ApiResponse<TeamDto> addPlayerToTeam(Long teamId, Long selfPlayerId) {
        try {
            Player player = playerRepository.findById(selfPlayerId)
                    .orElseThrow(() -> new Exception("Игрок не найден"));

            Team team = teamRepository.findById(teamId)
                    .orElseThrow(() -> new Exception("Команда не найдена"));

            if (team.getStatus() != TeamStatus.ACTIVE) {
                return new ApiResponse<>("В эту команду сейчас нельзя вступить", null, true);
            }

            Optional<TeamPlayer> existing = teamPlayerRepository.findByPlayerIdAndTeam_GameIdAndStatusIn(
                    player.getId(),
                    team.getGameId(),
                    List.of(
                            TeamPlayerStatus.ACTIVE,
                            TeamPlayerStatus.REQUESTED,
                            TeamPlayerStatus.INVITED
                    )
            );
            if (existing.isPresent()) {
                return new ApiResponse<>("Игрок уже состоит в другой команде этой игры", null, true);
            }

            TeamPlayer tp = new TeamPlayer();
            tp.setId(new TeamPlayerId(player.getId(), team.getId()));
            tp.setPlayer(player);
            tp.setTeam(team);
            tp.setCaptain(false);

            if (team.getType() == TeamType.PUBLIC) {
                tp.setStatus(TeamPlayerStatus.ACTIVE);
            } else {
                tp.setStatus(TeamPlayerStatus.REQUESTED);
            }

            teamPlayerRepository.save(tp);
            if (team.getType() == TeamType.PUBLIC) {
                ApiResponse<Void> checkTeamCorrectResponse = gameClientService.checkTeamCorrect(getTeamWithPlayers(team.getId()).getData());
                if (checkTeamCorrectResponse.isError()){
                    TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                    return new ApiResponse<>(checkTeamCorrectResponse.getMessage(), null, true);
                }
            }
            return new ApiResponse<>("Игрок добавлен в команду", getTeamWithPlayers(teamId).getData(), false);
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new ApiResponse<>("Ошибка при добавлении в команду: " + ex.getMessage(), null, true);
        }
    }

    // ------------------ Принятие/отклонение заявки ------------------
    @Transactional
    public ApiResponse<TeamDto> handleRequest(Long teamId, Long playerId, Long selfPlayerId, boolean approve) {
        try {
            TeamPlayer captain = teamPlayerRepository.findById(new TeamPlayerId(selfPlayerId, teamId))
                    .orElseThrow(() -> new Exception("Вы не состоите в команде"));

            if (!captain.isCaptain()) {
                return new ApiResponse<>("Вы не капитан команды", null, true);
            }

            TeamPlayer tp = teamPlayerRepository.findById(new TeamPlayerId(playerId, teamId))
                    .orElseThrow(() -> new Exception("Заявка не найдена"));

            if (tp.getStatus() != TeamPlayerStatus.REQUESTED && tp.getStatus() != TeamPlayerStatus.INVITED) {
                return new ApiResponse<>("Невозможно обработать заявку", null, true);
            }

            tp.setStatus(approve ? TeamPlayerStatus.ACTIVE : TeamPlayerStatus.CANCELED);
            teamPlayerRepository.save(tp);

            ApiResponse<Void> checkTeamCorrectResponse = gameClientService.checkTeamCorrect(getTeamWithPlayers(tp.getTeam().getId()).getData());
            if (checkTeamCorrectResponse.isError()){
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return new ApiResponse<>(checkTeamCorrectResponse.getMessage(), null, true);
            }
            return new ApiResponse<>("Заявка обработана", getTeamWithPlayers(teamId).getData(), false);
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new ApiResponse<>("Ошибка при обработке заявки: " + ex.getMessage(), null, true);
        }
    }

    // ------------------ Генерация пригласительной ссылки ------------------
    public ApiResponse<List<TeamPlayerSummaryDto>> getPendingRequests(Long teamId, Long selfPlayerId) {
        try {
            TeamPlayer captain = teamPlayerRepository.findById(new TeamPlayerId(selfPlayerId, teamId))
                    .orElseThrow(() -> new Exception("Вы не состоите в команде"));

            if (!captain.isCaptain()) {
                return new ApiResponse<>("Вы не капитан команды", null, true);
            }

            List<TeamPlayerSummaryDto> pendingPlayers = teamPlayerRepository.findByTeamIdAndStatusIn(
                            teamId,
                            List.of(TeamPlayerStatus.REQUESTED, TeamPlayerStatus.INVITED)
                    ).stream()
                    .map(tp -> new TeamPlayerSummaryDto(
                            tp.getPlayer().getId(),
                            tp.getPlayer().getNickname(),
                            tp.isCaptain(),
                            tp.getStatus()
                    ))
                    .toList();

            return new ApiResponse<>("Заявки найдены", pendingPlayers, false);
        } catch (Exception ex) {
            return new ApiResponse<>("Ошибка при получении заявок: " + ex.getMessage(), null, true);
        }
    }

    public String generateInviteLink(Long teamId, Long selfPlayerId) throws Exception {
        TeamPlayer captain = requireActiveCaptain(teamId, selfPlayerId);

        if (captain.getTeam().getStatus() != TeamStatus.ACTIVE) {
            throw new Exception("Ссылку приглашения можно создать только для активной команды");
        }

        byte[] bytes = new byte[16];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        // Храним токен в Redis на 1 час
        redisTemplate.opsForValue().set("invite:" + token, teamId, Duration.ofHours(24));
        return token;
    }

    // ------------------ Присоединение по токену ------------------
    @Transactional
    public ApiResponse<TeamDto> joinByInviteToken(String token, Long selfPlayerId) {
        try {
            Object rawTeamId = redisTemplate.opsForValue().get("invite:" + token);
            Long teamId = null;

            if (rawTeamId instanceof Number number) {
                teamId = number.longValue();
            } else if (rawTeamId instanceof String value && !value.isBlank()) {
                teamId = Long.parseLong(value);
            }

            if (teamId == null) throw new Exception("Недействительная ссылка");

            Player player = playerRepository.findById(selfPlayerId)
                    .orElseThrow(() -> new Exception("Игрок не найден"));

            Team team = teamRepository.findById(teamId)
                    .orElseThrow(() -> new Exception("Команда не найдена"));

            if (team.getStatus() != TeamStatus.ACTIVE) {
                return new ApiResponse<>("Эта команда сейчас не принимает новых игроков", null, true);
            }

            Optional<TeamPlayer> existing = teamPlayerRepository.findByPlayerIdAndTeam_GameIdAndStatusIn(
                    player.getId(),
                    team.getGameId(),
                    List.of(
                            TeamPlayerStatus.ACTIVE,
                            TeamPlayerStatus.REQUESTED,
                            TeamPlayerStatus.INVITED
                    )
            );

            if (existing.isPresent()) {
                TeamPlayer existingMembership = existing.get();

                if (!existingMembership.getTeam().getId().equals(teamId)) {
                    return new ApiResponse<>("Игрок уже состоит в другой команде этой игры", null, true);
                }

                if (existingMembership.getStatus() == TeamPlayerStatus.ACTIVE) {
                    return new ApiResponse<>("Игрок уже состоит в этой команде", getTeamWithPlayers(teamId).getData(), false);
                }

                existingMembership.setStatus(TeamPlayerStatus.ACTIVE);
                teamPlayerRepository.save(existingMembership);
            } else {
                TeamPlayer tp = new TeamPlayer();
                tp.setId(new TeamPlayerId(player.getId(), team.getId()));
                tp.setPlayer(player);
                tp.setTeam(team);
                tp.setCaptain(false);
                tp.setStatus(TeamPlayerStatus.ACTIVE);
                teamPlayerRepository.save(tp);
            }

            ApiResponse<Void> checkTeamCorrectResponse = gameClientService.checkTeamCorrect(getTeamWithPlayers(teamId).getData());
            if (checkTeamCorrectResponse.isError()) {
                TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
                return new ApiResponse<>(checkTeamCorrectResponse.getMessage(), null, true);
            }

            return new ApiResponse<>("Игрок присоединился по приглашению", getTeamWithPlayers(teamId).getData(), false);
        } catch (Exception ex) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
            return new ApiResponse<>("Ошибка при присоединении по ссылке: " + ex.getMessage(), null, true);
        }
    }

    // ------------------ Удаление/выход игрока ------------------
    public ApiResponse<TeamDto> removePlayerFromTeam(Long teamId, Long playerId, Long selfPlayerId, boolean kicked) {
        try {
            TeamPlayer requester = teamPlayerRepository.findById(new TeamPlayerId(selfPlayerId, teamId))
                    .orElseThrow(() -> new Exception("Вы не состоите в команде"));

            TeamPlayer tp = teamPlayerRepository.findById(new TeamPlayerId(playerId, teamId))
                    .orElseThrow(() -> new Exception("Игрок не состоит в команде"));

            // проверка прав
            if (!requester.isCaptain() && selfPlayerId != playerId) {
                return new ApiResponse<>("Нет прав на удаление игрока", null, true);
            }

            if (tp.isCaptain()) {
                // капитан сам выходит
                List<TeamPlayer> others = getActiveTeamMembers(teamId).stream()
                        .filter(member -> !Objects.equals(member.getPlayer().getId(), playerId))
                        .toList();
                if (others.isEmpty()) {
                    // никто не остался — удаляем команду
                    Team team = teamRepository.findById(teamId).orElseThrow();
                    team.setStatus(TeamStatus.DELETED);
                    teamRepository.save(team);
                    tp.setStatus(TeamPlayerStatus.LEAVED);
                    teamPlayerRepository.save(tp);
                    return new ApiResponse<>("Капитан вышел, команда удалена", getTeamWithPlayers(teamId).getData(), false);
                } else {
                    // рандомно новый капитан
                    TeamPlayer newCaptain = others.get(random.nextInt(others.size()));
                    newCaptain.setCaptain(true);
                    teamPlayerRepository.save(newCaptain);

                    tp.setStatus(TeamPlayerStatus.LEAVED);
                    teamPlayerRepository.save(tp);
                    return new ApiResponse<>("Капитан вышел, назначен новый капитан", getTeamWithPlayers(teamId).getData(), false);
                }
            } else {
                tp.setStatus(kicked ? TeamPlayerStatus.KICKED : TeamPlayerStatus.LEAVED);
                teamPlayerRepository.save(tp);
                return new ApiResponse<>("Игрок удалён из команды", getTeamWithPlayers(teamId).getData(), false);
            }

        } catch (Exception ex) {
            return new ApiResponse<>("Ошибка при удалении игрока: " + ex.getMessage(), null, true);
        }
    }

    // ------------------ Передача капитана ------------------
    public ApiResponse<TeamDto> transferCaptain(Long teamId, Long newCaptainId, Long selfPlayerId) {
        try {
            TeamPlayer requester = teamPlayerRepository.findById(new TeamPlayerId(selfPlayerId, teamId))
                    .orElseThrow(() -> new Exception("Вы не состоите в команде"));

            if (!requester.isCaptain()) return new ApiResponse<>("Вы не капитан", null, true);

            TeamPlayer newCaptain = teamPlayerRepository.findById(new TeamPlayerId(newCaptainId, teamId))
                    .orElseThrow(() -> new Exception("Игрок не состоит в команде"));

            List<TeamPlayer> players = teamPlayerRepository.findByTeamId(teamId);
            for (TeamPlayer tp : players) tp.setCaptain(tp.getPlayer().getId().equals(newCaptainId));
            teamPlayerRepository.saveAll(players);

            return new ApiResponse<>("Капитан передан", getTeamWithPlayers(teamId).getData(), false);
        } catch (Exception ex) {
            return new ApiResponse<>("Ошибка при передаче капитана: " + ex.getMessage(), null, true);
        }
    }

    // ------------------ Получение команды с ACTIVE игроками ------------------
    public ApiResponse<TeamDto> updateTeamStatus(Long teamId, TeamStatus status, Long selfPlayerId) {
        try {
            TeamPlayer captain = requireActiveCaptain(teamId, selfPlayerId);
            Team team = captain.getTeam();

            if (status == TeamStatus.ACTIVE && getActiveTeamMembers(teamId).isEmpty()) {
                return new ApiResponse<>("Нельзя активировать пустую команду", null, true);
            }

            team.setStatus(status);
            teamRepository.save(team);

            return new ApiResponse<>("Статус команды обновлён", getTeamWithPlayers(teamId).getData(), false);
        } catch (Exception ex) {
            return new ApiResponse<>("Ошибка при обновлении статуса команды: " + ex.getMessage(), null, true);
        }
    }

    public ApiResponse<TeamDto> getTeamWithPlayers(Long teamId) {
        try {
            Team team = teamRepository.findById(teamId)
                    .orElseThrow(() -> new Exception("Команда не найдена"));

            List<TeamPlayer> members = teamPlayerRepository.findByTeamId(teamId);
            List<TeamPlayer> activeMembers = members.stream()
                    .filter(tp -> tp.getStatus() == TeamPlayerStatus.ACTIVE)
                    .toList();

            return new ApiResponse<>("Команда найдена", mapToTeamDto(team, activeMembers), false);
        } catch (Exception ex) {
            return new ApiResponse<>("Ошибка при получении команды: " + ex.getMessage(), null, true);
        }
    }

    // ------------------ Получение команд с ACTIVE игроками ------------------
    public ApiResponse<List<TeamDto>> getTeamsWithPlayersByIds(List<Long> teamIds) {
        try {
            if (teamIds == null || teamIds.isEmpty()) {
                return new ApiResponse<>("Список ID команд пуст", null, true);
            }

            List<Team> teams = teamRepository.findAllById(teamIds);

            if (teams.isEmpty()) {
                return new ApiResponse<>("Команды не найдены", null, true);
            }

            // Получаем всех игроков для всех команд одним запросом
            List<TeamPlayer> allTeamPlayers = teamPlayerRepository.findByTeamIdIn(teamIds);

            // Группируем игроков по командам и фильтруем только ACTIVE
            Map<Long, List<TeamPlayer>> playersByTeamId = allTeamPlayers.stream()
                    .filter(tp -> tp.getStatus() == TeamPlayerStatus.ACTIVE)
                    .collect(Collectors.groupingBy(tp -> tp.getTeam().getId()));

            List<TeamDto> teamDtos = teams.stream()
                    .map(team -> {
                        List<TeamPlayer> activeMembers = playersByTeamId.getOrDefault(team.getId(), Collections.emptyList());
                        return mapToTeamDto(team, activeMembers);
                    })
                    .collect(Collectors.toList());

            return new ApiResponse<>("Команды найдены", teamDtos, false);
        } catch (Exception ex) {
            return new ApiResponse<>("Ошибка при получении команд: " + ex.getMessage(), null, true);
        }
    }

    public ApiResponse<List<TeamDto>> getAllTeamsWithPlayers() {
        try {
            List<Team> teams = teamRepository.findAll().stream()
                    .filter(team -> team.getStatus() == TeamStatus.ACTIVE)
                    .toList();

            if (teams.isEmpty()) {
                return new ApiResponse<>("Команды не найдены", Collections.emptyList(), false);
            }

            List<Long> teamIds = teams.stream()
                    .map(Team::getId)
                    .toList();

            List<TeamPlayer> allTeamPlayers = teamPlayerRepository.findByTeamIdIn(teamIds);

            Map<Long, List<TeamPlayer>> playersByTeamId = allTeamPlayers.stream()
                    .filter(tp -> tp.getStatus() == TeamPlayerStatus.ACTIVE)
                    .collect(Collectors.groupingBy(tp -> tp.getTeam().getId()));

            List<TeamDto> teamDtos = teams.stream()
                    .map(team -> {
                        List<TeamPlayer> activeMembers = playersByTeamId.getOrDefault(team.getId(), Collections.emptyList());
                        return mapToTeamDto(team, activeMembers);
                    })
                    .toList();

            return new ApiResponse<>("Команды найдены", teamDtos, false);
        } catch (Exception ex) {
            return new ApiResponse<>("Ошибка при получении команд: " + ex.getMessage(), null, true);
        }
    }

    public ApiResponse<List<TeamDto>> searchTeams(String query) {
        try {
            String key = "search:teams:" + query.toLowerCase();

            // 1️⃣ Проверяем кеш
            List<TeamDto> cachedTeams = (List<TeamDto>) redisTemplate.opsForValue().get(key);
            if (cachedTeams != null) {
                return new ApiResponse<>("Команды найдены (из кеша)", cachedTeams, false);
            }

            // 2️⃣ Ищем в базе
            List<TeamDto> teams = teamRepository
                    .findByNameContainingIgnoreCase(query)
                    .stream()
                    .filter(team -> team.getStatus() == TeamStatus.ACTIVE)
                    .map(team -> {
                        List<TeamPlayer> members = teamPlayerRepository.findByTeamId(team.getId())
                                .stream()
                                .filter(tp -> tp.getStatus() == TeamPlayerStatus.ACTIVE)
                                .toList();
                        return mapToTeamDto(team, members);
                    })
                    .toList();

            // 3️⃣ Сохраняем в кеш (TTL 5 минут)
            redisTemplate.opsForValue().set(key, teams, Duration.ofMinutes(5));

            return new ApiResponse<>("Команды найдены", teams, false);

        } catch (Exception ex) {
            return new ApiResponse<>("Ошибка поиска команд: " + ex.getMessage(), null, true);
        }
    }

    private TeamPlayer requireActiveCaptain(Long teamId, Long selfPlayerId) throws Exception {
        TeamPlayer membership = teamPlayerRepository.findById(new TeamPlayerId(selfPlayerId, teamId))
                .orElseThrow(() -> new Exception("Вы не состоите в этой команде"));

        if (membership.getStatus() != TeamPlayerStatus.ACTIVE) {
            throw new Exception("Управлять командой может только действующий участник");
        }

        if (!membership.isCaptain()) {
            throw new Exception("Управлять статусом команды может только капитан");
        }

        return membership;
    }

    private List<TeamPlayer> getActiveTeamMembers(Long teamId) {
        return teamPlayerRepository.findByTeamId(teamId).stream()
                .filter(member -> member.getStatus() == TeamPlayerStatus.ACTIVE)
                .toList();
    }

    private TeamDto mapToTeamDto(Team team, List<TeamPlayer> members) {
        List<TeamPlayerSummaryDto> playerDtos = members.stream()
                .map(tp -> new TeamPlayerSummaryDto(tp.getPlayer().getId(),
                        tp.getPlayer().getNickname(),
                        tp.isCaptain(),
                        tp.getStatus()))
                .toList();

        return new TeamDto(team.getId(), team.getGameId(), team.getName(), team.getStatus(), team.getType(), playerDtos);
    }
}
