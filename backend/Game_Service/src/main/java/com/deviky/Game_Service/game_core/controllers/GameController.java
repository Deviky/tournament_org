package com.deviky.Game_Service.game_core.controllers;

import com.deviky.Game_Service.game_core.dto.*;
import com.deviky.Game_Service.game_core.services.GameService;
import com.deviky.Game_Service.game_core.models.GameEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/game")
@RequiredArgsConstructor
public class GameController {
    private final GameService gameService;

    @GetMapping("/public/get/{gameId}")
    public ResponseEntity<ApiResponse<GameDto>> getGame(
            @PathVariable int gameId
    ) {
        ApiResponse<GameDto> response = gameService.getGame(gameId);
        HttpStatus status = response.isError() ? HttpStatus.NOT_FOUND : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/public/get_all")
    public ResponseEntity<ApiResponse<List<GameEntity>>> getGames(
    ) {
        ApiResponse<List<GameEntity>> response = gameService.getAllGames();
        HttpStatus status = response.isError() ? HttpStatus.NOT_FOUND : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    @PostMapping("/private/check_player")
    public ResponseEntity<ApiResponse<Void>> checkPlayer(
            @RequestBody Player player
    ) {
        ApiResponse<Void> response = gameService.checkPlayerIsCorrect(player);
        HttpStatus status = response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    @PostMapping("/private/check_team")
    public ResponseEntity<ApiResponse<Void>> checkTeam(
            @RequestBody Team team
    ) {
        ApiResponse<Void> response = gameService.checkTeamIsCorrect(team);
        HttpStatus status = response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    @PostMapping("/private/check_tournament_create")
    public ResponseEntity<ApiResponse<Void>> checkTournamentCreate(
            @RequestBody Tournament tournament
    ) {
        ApiResponse<Void> response = gameService.checkTournamentCreateIsCorrect(tournament);
        HttpStatus status = response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    @PostMapping("/private/check_tournament_start")
    public ResponseEntity<ApiResponse<Void>> checkTournamentStart(
            @RequestBody Tournament tournament
    ) {
        ApiResponse<Void> response = gameService.checkTournamentStartIsCorrect(tournament);
        HttpStatus status = response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/private/get_bracket_algorithms/{gameId}")
    public ResponseEntity<ApiResponse<List<String>>> getBracketAlgorithms(
            @PathVariable Integer gameId
    ) {
        ApiResponse<List<String>> response = gameService.getAlgorithmsSupported(gameId);
        HttpStatus status = response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }
}
