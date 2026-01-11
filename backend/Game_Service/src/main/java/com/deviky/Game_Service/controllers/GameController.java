package com.deviky.Game_Service.controllers;

import com.deviky.Game_Service.dto.ApiResponse;
import com.deviky.Game_Service.dto.Player;
import com.deviky.Game_Service.models.GameEntity;
import com.deviky.Game_Service.services.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    // Проверка корректности игрока
    @PostMapping("/check-player")
    public ResponseEntity<ApiResponse<Void>> checkPlayer(
            @RequestBody Player player
    ) {
        ApiResponse<Void> response = gameService.checkPlayerIsCorrect(player);
        HttpStatus status = response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

}
