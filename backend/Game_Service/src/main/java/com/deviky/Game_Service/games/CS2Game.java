package com.deviky.Game_Service.games;

import com.deviky.Game_Service.game_core.dto.PlayerGameInfo;
import com.deviky.Game_Service.game_core.dto.Team;
import com.deviky.Game_Service.game_core.dto.Tournament;
import com.deviky.Game_Service.game_core.models.*;

import java.util.List;
import java.util.Map;

@GameType(name = "Counter-Strike 2")
public class CS2Game extends Game {
    @GameParamField(param_name = "max_players_team")
    private int maxPlayersTeam;

    @GameParamField(param_name = "player_game_platform_needed")
    private Map<String, String> playerGamePlatformNeeded;

    @GameParamField(param_name = "player_statistic_platform_supported")
    private String playerStatisticPlatformSupported;

    @GameParamField(param_name = "min_teams_tournament")
    private int minTeamsTournament;

    public CS2Game(List<GameParam> gameParams) {
        super(gameParams);
    }

    @Override
    public CheckResult isPlayerCorrect(PlayerGameInfo playerGameInfo) {
        if (playerGameInfo == null) {
            return new CheckResult(true, "Игрок не связан с данной игрой");
        }

        Map<String, String> links = playerGameInfo.getLinks();

        // Проверяем обязательные платформы
        for (Map.Entry<String, String> entry : playerGamePlatformNeeded.entrySet()) {
            String platform = entry.getKey();
            String expectedDomain = entry.getValue(); // ожидаемый url/domain из параметра

            // Получаем ссылку игрока для этой платформы
            String playerLink = links.get(platform);
            if (playerLink == null || playerLink.isBlank()) {
                return new CheckResult(true, "Отсутствует привязка к платформе " + platform);
            }

            // Проверяем, содержит ли ссылка игрока ожидаемый домен
            if (!playerLink.contains(expectedDomain)) {
                return new CheckResult(true, "Домен игрока не совпадает с доменом платформы " + expectedDomain);
            }
        }

        return new CheckResult(false, "");
    }

    @Override
    public CheckResult isTeamCorrect(Team team) {
        if (team.getPlayers().size() > maxPlayersTeam)
            return new CheckResult(false, "");
        else
            return new CheckResult(true, "Кол-во игроков в команде превышает " + maxPlayersTeam);
    }

    @Override
    public CheckResult isTournamentCreateCorrect(Tournament tournament) {
        Integer min_teams = tournament.getMinTeams();
        if (min_teams == null)
            return new CheckResult(true, "Должно быть указано минимальное кол-во игроков");
        else
            if (min_teams < minTeamsTournament)
                return new CheckResult(true, "Минимальное кол-во команд на турнире должно быть не менее " + minTeamsTournament);
            else
                return new CheckResult(false, "");
    }

    @Override
    public CheckResult isTournamentStartCorrect(Tournament tournament) {
        int teamsCount = tournament.getTeams().size();
        if (teamsCount < minTeamsTournament)
            return new CheckResult(true, "На турнире должно быть зарегистрированно не менее " + minTeamsTournament + " команд");
        return new CheckResult();
    }
}
