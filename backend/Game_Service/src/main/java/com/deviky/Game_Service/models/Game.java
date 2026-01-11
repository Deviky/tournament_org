package com.deviky.Game_Service.models;

import com.deviky.Game_Service.dto.Team;
import com.deviky.Game_Service.dto.Tournament;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import java.lang.reflect.Type;

public abstract class Game {

    @GameParamField(param_name = "bracket_algorithm")
    private List<String> bracketAlgorithms;
    @GameParamField(param_name = "min_players_team")
    private int minPlayersTeam;
    @GameParamField(param_name = "max_players_team")
    private int maxPlayersTeam;

    public Game(List<GameParam> gameParams) {
        Map<String, List<GameParam>> grouped =
                gameParams.stream()
                        .collect(Collectors.groupingBy(GameParam::getParamName));

        for (Field field : this.getClass().getDeclaredFields()) {
            GameParamField ann = field.getAnnotation(GameParamField.class);
            if (ann == null) continue;

            String paramName = ann.param_name();
            List<GameParam> values = grouped.get(paramName);
            if (values == null || values.isEmpty()) continue;

            field.setAccessible(true);

            try {
                if (List.class.isAssignableFrom(field.getType())) {
                    Class<?> elementType = getListGenericType(field);
                    List<Object> list = values.stream()
                            .sorted(Comparator.comparingInt(GameParam::getApplyRn))
                            .map(GameParam::getParamValue)
                            .map(v -> cast(v, elementType))
                            .toList();

                    field.set(this, list);
                } else {
                    field.set(this, cast(values.get(0).getParamValue(), field.getType()));
                }
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private Class<?> getListGenericType(Field field) {
        Type type = field.getGenericType();
        if (!(type instanceof ParameterizedType pt)) {
            throw new IllegalStateException(
                    "List field must be parameterized: " + field.getName()
            );
        }
        return (Class<?>) pt.getActualTypeArguments()[0];
    }

    protected Object cast(String value, Class<?> type) {
        if (type == int.class || type == Integer.class)
            return Integer.parseInt(value);
        if (type == long.class || type == Long.class)
            return Long.parseLong(value);
        if (type == boolean.class || type == Boolean.class)
            return Boolean.parseBoolean(value);
        if (type == double.class || type == Double.class)
            return Double.parseDouble(value);
        if (type == String.class)
            return value;

        throw new IllegalArgumentException("Unsupported param type: " + type);
    }

    public abstract boolean isTeamCorrect(Team team);
    public abstract boolean isTournamentCreateCorrect(Tournament tournament);
    public abstract boolean isTournamentStartCorrect(Tournament tournament);
    public abstract List<String> getBracketAlgorithmsSupported();
}