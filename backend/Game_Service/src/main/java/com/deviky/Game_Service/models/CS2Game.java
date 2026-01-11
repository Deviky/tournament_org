package com.deviky.Game_Service.models;

import com.deviky.Game_Service.dto.Team;
import com.deviky.Game_Service.dto.Tournament;

import java.util.List;

@GameType(name = "Counter-Strike 2")
public class CS2Game extends Game{

    public CS2Game(List<GameParam> gameParams) {
        super(gameParams);
    }

    @Override
    public boolean isTeamCorrect(Team team) {
        return false;
    }

    @Override
    public boolean isTournamentCreateCorrect(Tournament tournament) {
        return false;
    }

    @Override
    public boolean isTournamentStartCorrect(Tournament tournament) {
        return false;
    }

    @Override
    public List<String> getBracketAlgorithmsSupported() {
        return null;
    }

}
