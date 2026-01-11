package com.deviky.Game_Service.models;

import com.deviky.Game_Service.dto.Player;
import com.deviky.Game_Service.dto.PlayerGameInfo;
import com.deviky.Game_Service.dto.Team;
import com.deviky.Game_Service.dto.Tournament;

import java.util.List;
import java.util.Map;

@GameType(name = "Counter-Strike 2")
public class CS2Game extends Game{

    @GameParamField(param_name = "player_game_platform_supported")
    private Map<String, String> playerGamePlatformSupported;

    @GameParamField(param_name = "player_statistic_platform_supported")
    private String playerStatisticPlatformSupported;

    public CS2Game(List<GameParam> gameParams) {
        super(gameParams);
    }

    @Override
    public CheckResult isPlayerCorrect(PlayerGameInfo playerGameInfo) {
        if (playerGameInfo == null) {
            return new CheckResult(false, "Игрок не связан с данной игрой");
        }

        Map<String, String> links = playerGameInfo.getLinks();

        // Проверяем обязательные платформы
        for (Map.Entry<String, String> entry : playerGamePlatformSupported.entrySet()) {
            String platform = entry.getKey();
            String expectedDomain = entry.getValue(); // ожидаемый url/domain из параметра

            // Получаем ссылку игрока для этой платформы
            String playerLink = links.get(platform);
            if (playerLink == null || playerLink.isBlank()) {
                return new CheckResult(false, "Отсутствует привязка к платформе " + platform);
            }

            // Проверяем, содержит ли ссылка игрока ожидаемый домен
            if (!playerLink.contains(expectedDomain)) {
                return new CheckResult(false, "Домен игрока не совпадает с доменом платформы " + expectedDomain);
            }
        }

        return new CheckResult(true, "");
    }

    @Override
    public CheckResult isTeamCorrect(Team team) {
        return new CheckResult();
    }

    @Override
    public CheckResult isTournamentCreateCorrect(Tournament tournament) {
        return new CheckResult();
    }

    @Override
    public CheckResult isTournamentStartCorrect(Tournament tournament) {
        return new CheckResult();
    }

    @Override
    public List<String> getBracketAlgorithmsSupported() {
        return null;
    }

}
