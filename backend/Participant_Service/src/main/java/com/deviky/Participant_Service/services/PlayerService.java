package com.deviky.Participant_Service.services;

import com.deviky.Participant_Service.dto.*;
import com.deviky.Participant_Service.models.*;
import com.deviky.Participant_Service.repositories.PlayerRepository;
import com.deviky.Participant_Service.repositories.TeamPlayerRepository;
import com.deviky.Participant_Service.repositories.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import com.deviky.Participant_Service.models.Player;
import com.deviky.Participant_Service.models.TeamPlayer;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;
    private final TeamPlayerRepository teamPlayerRepository;
    private final TeamRepository teamRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    public ApiResponse<PlayerDto> createPlayer(CreatePlayerRequest dto) {
        try {
            Player player = new Player();
            player.setId(dto.getId());
            player.setNickname(dto.getNickname());
            player.setLinks(dto.getLinks());
            Player saved = playerRepository.save(player);
            return new ApiResponse<>("Игрок создан", mapToPlayerDto(saved), false);
        } catch (Exception ex) {
            return new ApiResponse<>("Ошибка при создании игрока: " + ex.getMessage(), null, true);
        }
    }

    public ApiResponse<PlayerDto> getPlayerWithTeams(Long playerId, Long selfPlayerId) {
        try {
            Player player = playerRepository.findById(playerId)
                    .orElseThrow(() -> new Exception("Игрок не найден"));
            List<TeamPlayer> memberships = teamPlayerRepository.findByPlayerId(playerId);
            return new ApiResponse<>("Игрок найден", mapToPlayerDto(player, memberships), false);
        } catch (Exception ex) {
            return new ApiResponse<>(ex.getMessage(), null, true);
        }
    }

    public ApiResponse<List<PlayerDto>> searchPlayers(String query) {
        try {
            List<PlayerDto> players = playerRepository
                    .findByNicknameContainingIgnoreCase(query)
                    .stream()
                    .map(this::mapToPlayerDto)
                    .toList();

            return new ApiResponse<>(
                    "Игроки найдены",
                    players,
                    false
            );
        } catch (Exception ex) {
            return new ApiResponse<>("Ошибка поиска игроков: " + ex.getMessage(), null, true);
        }
    }

    private PlayerDto mapToPlayerDto(Player player) {
        List<TeamPlayer> memberships = teamPlayerRepository.findByPlayerId(player.getId());
        return mapToPlayerDto(player, memberships);
    }

    private PlayerDto mapToPlayerDto(Player player, List<TeamPlayer> memberships) {
        List<TeamSummaryDto> teams = memberships.stream()
                .filter(tp -> tp.getStatus() == TeamPlayerStatus.ACTIVE)
                .map(tp -> new TeamSummaryDto(tp.getTeam().getId(),
                        tp.getTeam().getGameId(),
                        tp.getTeam().getName(),
                        tp.getTeam().getStatus(),
                        tp.getTeam().getType()))
                .toList();
        return new PlayerDto(player.getId(), player.getNickname(), player.getLinks(), teams);
    }
}

