package com.deviky.Participant_Service.controllers;

import com.deviky.Participant_Service.dto.*;
import com.deviky.Participant_Service.services.PlayerService;
import com.deviky.Participant_Service.services.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/participant/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;
    private final TeamService teamService;

    // ------------------ Создание игрока ------------------
    @PostMapping
    public ResponseEntity<ApiResponse<PlayerDto>> createPlayer(
            @RequestBody CreatePlayerRequest dto
    ) {
        ApiResponse<PlayerDto> response = playerService.createPlayer(dto);
        HttpStatus status = response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    // ------------------ Получение игрока с командами ------------------
    @GetMapping("/{playerId}")
    public ResponseEntity<ApiResponse<PlayerDto>> getPlayer(
            @PathVariable Long playerId,
            @RequestHeader("X-Self-Player-Id") Long selfPlayerId
    ) {
        ApiResponse<PlayerDto> response = playerService.getPlayerWithTeams(playerId, selfPlayerId);
        HttpStatus status = response.isError() ? HttpStatus.NOT_FOUND : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    // ------------------ Поиск игроков ------------------
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<SearchDto>> searchPlayers(
            @RequestParam String query
    ) {
        ApiResponse<List<PlayerDto>> responsePlayer = playerService.searchPlayers(query);
        ApiResponse<List<TeamDto>> responseTeam = teamService.searchTeams(query);
        HttpStatus status = responsePlayer.isError() || responseTeam.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
        SearchDto searchDto = new SearchDto(responsePlayer.getData(), responseTeam.getData());
        return ResponseEntity.status(status).body(new ApiResponse<SearchDto>(
                responsePlayer.isError() || responseTeam.isError() ? "Ошибка сервера" : "",
                searchDto,
                responsePlayer.isError() || responseTeam.isError()));
    }
}
