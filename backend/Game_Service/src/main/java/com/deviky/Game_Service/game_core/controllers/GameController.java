package com.deviky.Game_Service.game_core.controllers;

import com.deviky.Game_Service.game_core.dto.ApiResponse;
import com.deviky.Game_Service.game_core.dto.Player;
import com.deviky.Game_Service.game_core.dto.Team;
import com.deviky.Game_Service.game_core.dto.Tournament;
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

    @GetMapping("/{gameId}")
    public ResponseEntity<ApiResponse<GameEntity>> getGame(
            @PathVariable int gameId
    ) {
        ApiResponse<GameEntity> response = gameService.getGame(gameId);
        HttpStatus status = response.isError() ? HttpStatus.NOT_FOUND : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<GameEntity>>> getGame(
    ) {
        ApiResponse<List<GameEntity>> response = gameService.getAllGames();
        HttpStatus status = response.isError() ? HttpStatus.NOT_FOUND : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    @PostMapping("/check-player")
    public ResponseEntity<ApiResponse<Void>> checkPlayer(
            @RequestBody Player player
    ) {
        ApiResponse<Void> response = gameService.checkPlayerIsCorrect(player);
        HttpStatus status = response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    @PostMapping("/check-team")
    public ResponseEntity<ApiResponse<Void>> checkTeam(
            @RequestBody Team team
    ) {
        ApiResponse<Void> response = gameService.checkTeamIsCorrect(team);
        HttpStatus status = response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    @PostMapping("/check-tournament-create")
    public ResponseEntity<ApiResponse<Void>> checkTournamentCreate(
            @RequestBody Tournament tournament
    ) {
        ApiResponse<Void> response = gameService.checkTournamentCreateIsCorrect(tournament);
        HttpStatus status = response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    @PostMapping("/check-tournament-start")
    public ResponseEntity<ApiResponse<Void>> checkTournamentStart(
            @RequestBody Tournament tournament
    ) {
        ApiResponse<Void> response = gameService.checkTournamentStartIsCorrect(tournament);
        HttpStatus status = response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/get-bracket-algorithms/{gameId}")
    public ResponseEntity<ApiResponse<List<String>>> getBracketAlgorithms(
            @PathVariable Integer gameId
    ) {
        ApiResponse<List<String>> response = gameService.getAlgorithmsSupported(gameId);
        HttpStatus status = response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }
}
