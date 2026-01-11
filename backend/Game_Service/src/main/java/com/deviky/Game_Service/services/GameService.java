package com.deviky.Game_Service.services;

import com.deviky.Game_Service.dto.ApiResponse;
import com.deviky.Game_Service.models.GameEntity;
import com.deviky.Game_Service.repositories.GameRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GameService {
    @Autowired
    private final GameRepository gameRepository;

    public ApiResponse<GameEntity> getGame(int gameId){
        try {
            GameEntity game = gameRepository.findById(gameId)
                    .orElseThrow(() -> new Exception("Игра не найдена"));
            return new ApiResponse<>("Игра найдена", game, false);
        } catch (Exception ex) {
            return new ApiResponse<>(ex.getMessage(), null, true);
        }
    }


}
