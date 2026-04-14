package com.deviky.Game_Service.game_core.services;

import com.deviky.Game_Service.game_core.dto.*;
import com.deviky.Game_Service.game_core.models.CheckResult;
import com.deviky.Game_Service.game_core.models.Game;
import com.deviky.Game_Service.game_core.models.GameEntity;
import com.deviky.Game_Service.game_core.models.GameType;
import com.deviky.Game_Service.game_core.repositories.GameRepository;
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
        String basePackage = "com.deviky.Game_Service.games";

        // Используем Reflections для поиска всех классов с аннотацией @GameType
        Reflections reflections = new Reflections(basePackage);
        Set<Class<?>> gameClasses = reflections.getTypesAnnotatedWith(GameType.class);

        for (Class<?> clazz : gameClasses) {
            GameType annotation = clazz.getAnnotation(GameType.class);
            if (annotation != null && annotation.name().equalsIgnoreCase(gameName)) {
                return (Game) clazz.getConstructor(List.class).newInstance(gameEntity.getGameParams());
            }
        }

        throw new Exception("Такой игры не существует");
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

    public ApiResponse<Void> checkTeamIsCorrect(Team team) {
        try {
            Game game = createGame(team.getGameId());
            CheckResult result = game.isTeamCorrect(team);
            return new ApiResponse<>(result.getMessage(), null, result.isError());
        } catch (Exception ex) {
            return new ApiResponse<>("Ошибка при проверке команды: " + ex.getMessage(), null, true);
        }
    }

    public ApiResponse<Void> checkTournamentCreateIsCorrect(Tournament tournament) {
        try {
            Game game = createGame(tournament.getGameId());
            CheckResult result = game.isTournamentCreateCorrect(tournament);
            return new ApiResponse<>(result.getMessage(), null, result.isError());
        } catch (Exception ex) {
            return new ApiResponse<>("Ошибка при проверке создания турнира: " + ex.getMessage(), null, true);
        }
    }

    public ApiResponse<Void> checkTournamentStartIsCorrect(Tournament tournament) {
        try {
            Game game = createGame(tournament.getGameId());
            CheckResult result = game.isTournamentStartCorrect(tournament);
            return new ApiResponse<>(result.getMessage(), null, result.isError());
        } catch (Exception ex) {
            return new ApiResponse<>("Ошибка при проверке начала турнира: " + ex.getMessage(), null, true);
        }
    }

    public ApiResponse<List<String>> getAlgorithmsSupported(Integer gameId){
        try {
            Game game = createGame(gameId);
            List<String> result = game.getBracketAlgorithmsSupported();
            return new ApiResponse<>("", result, false);
        } catch (Exception ex) {
            return new ApiResponse<>("Ошибка при взятии поддерживаемых алгоритмов для формирования турнирной сетки: " + ex.getMessage(), null, true);
        }
    }

    public ApiResponse<GameDto> getGame(Integer gameId){
        try {
            GameEntity game = gameRepository.findById(gameId)
                    .orElseThrow(() -> new Exception("Игра не найдена"));
            GameDto gameDto = GameDto.builder()
                    .id(game.getId())
                    .name(game.getName())
                    .description(game.getDescription())
                    .build();
            return new ApiResponse<>("Игра найдена", gameDto, false);
        } catch (Exception ex) {
            return new ApiResponse<>(ex.getMessage(), null, true);
        }
    }

    public ApiResponse<List<GameEntity>> getAllGames(){
        try {
            List<GameEntity> game = gameRepository.findAll();
            return new ApiResponse<>("Игра найдена", game, false);
        } catch (Exception ex) {
            return new ApiResponse<>(ex.getMessage(), null, true);
        }
    }
}
