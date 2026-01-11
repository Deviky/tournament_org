package com.deviky.Game_Service.services;

import com.deviky.Game_Service.dto.ApiResponse;
import com.deviky.Game_Service.dto.Player;
import com.deviky.Game_Service.dto.PlayerGameInfo;
import com.deviky.Game_Service.models.*;
import com.deviky.Game_Service.repositories.GameParamsRepository;
import com.deviky.Game_Service.repositories.GameRepository;
import lombok.RequiredArgsConstructor;
import org.reflections.Reflections;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class GameService {

    @Autowired
    private final GameRepository gameRepository;


    /**
     * Приватный метод: создаёт экземпляр класса игры по имени и аннотации @GameType
     */
    private Game createGame(int gameId) throws Exception {

        GameEntity gameEntity = gameRepository.findById(gameId).orElseThrow(() -> new Exception("Игра не найдена"));

        String gameName = gameEntity.getName();

        // Пакет, где хранятся классы игр
        String basePackage = "com.deviky.Game_Service.models";

        // Используем Reflections для поиска всех классов с аннотацией @GameType
        Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> gameClasses = reflections.getTypesAnnotatedWith(GameType.class);

        for (Class<?> clazz : gameClasses) {
            GameType annotation = clazz.getAnnotation(GameType.class);
            if (annotation != null && annotation.name().equalsIgnoreCase(gameName)) {
                return (Game) clazz.getConstructor(List.class).newInstance(gameEntity.getGameParams());
            }
        }

        throw new Exception("Класс игры с аннотацией @GameType для '" + gameName + "' не найден");
    }

    public ApiResponse<Void> checkPlayerIsCorrect(Player player) {
        try {
            for (PlayerGameInfo playerGameInfo : player.getGames()) {
                // Создаём объект игры по gameId
                Game game = createGame(playerGameInfo.getGameId());

                // Проверяем игрока через метод игры
                CheckResult result = game.isPlayerCorrect(playerGameInfo);

                if (result.isError()) {
                    // Если проверка не прошла — возвращаем ошибку с сообщением
                    return new ApiResponse<>(result.getMessage(), null, true);
                }
            }

            // Все проверки прошли успешно
            return new ApiResponse<>("Игрок корректен для всех игр", null, false);
        } catch (Exception ex) {
            return new ApiResponse<>("Ошибка при проверке игрока: " + ex.getMessage(), null, true);
        }
    }

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
