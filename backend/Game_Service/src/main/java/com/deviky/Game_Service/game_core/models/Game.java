package com.deviky.Game_Service.game_core.models;

import com.deviky.Game_Service.game_core.dto.PlayerGameInfo;
import com.deviky.Game_Service.game_core.dto.Team;
import com.deviky.Game_Service.game_core.dto.Tournament;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import java.lang.reflect.Type;

@Slf4j
public abstract class Game {

    @GameParamField(param_name = "bracket_algorithm")
    protected List<String> bracketAlgorithms;

    public Game(List<GameParam> gameParams) {
        log.debug("=== Game Constructor ===");
        log.debug("gameParams size: {}", gameParams != null ? gameParams.size() : 0);

        if (gameParams == null || gameParams.isEmpty()) {
            log.warn("gameParams is null or empty");
            this.bracketAlgorithms = new ArrayList<>();
            return;
        }

        Map<String, List<GameParam>> grouped =
                gameParams.stream()
                        .collect(Collectors.groupingBy(GameParam::getParamName));

        log.debug("Grouped keys: {}", grouped.keySet());

        // Собираем все поля: из текущего класса и из родительского
        List<Field> allFields = new ArrayList<>();
        allFields.addAll(List.of(this.getClass().getDeclaredFields()));
        allFields.addAll(List.of(this.getClass().getSuperclass().getDeclaredFields()));

        for (Field field : allFields) {
            GameParamField ann = field.getAnnotation(GameParamField.class);
            if (ann == null) continue;

            String paramName = ann.param_name();
            log.debug("Processing field: {}, param_name: {}", field.getName(), paramName);

            List<GameParam> values = grouped.get(paramName);
            if (values == null || values.isEmpty()) {
                log.debug("No values found for param_name: {}", paramName);
                continue;
            }

            log.debug("Found {} values for {}", values.size(), paramName);
            values.forEach(v -> log.debug("  - value={}, apply_rn={}", v.getParamValue(), v.getApplyRn()));

            field.setAccessible(true);

            try {
                if (List.class.isAssignableFrom(field.getType())) {
                    Class<?> elementType = getListGenericType(field);
                    log.debug("Field is List with element type: {}", elementType);

                    List<Object> list = values.stream()
                            .filter(v -> v.getApplyRn() != null)
                            .sorted(Comparator.comparingInt(GameParam::getApplyRn))
                            .map(GameParam::getParamValue)
                            .map(v -> cast(v, elementType))
                            .collect(Collectors.toList());

                    log.debug("Created list with {} elements: {}", list.size(), list);
                    field.set(this, list);
                } else {
                    log.debug("Field is not List, setting single value");
                    field.set(this, cast(values.get(0).getParamValue(), field.getType()));
                }
            } catch (IllegalAccessException e) {
                log.error("Error setting field: {}", field.getName(), e);
                throw new RuntimeException(e);
            }
        }

        log.debug("Final bracketAlgorithms: {}", this.bracketAlgorithms);
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

    private static final ObjectMapper mapper = new ObjectMapper();

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

        try {
            return mapper.readValue(value, type);
        } catch (Exception e) {
            throw new IllegalArgumentException(
                    "Cannot parse JSON to " + type.getName() + ": " + value,
                    e
            );
        }
    }

    public List<String> getBracketAlgorithmsSupported(){
        return bracketAlgorithms;
    }
    public abstract CheckResult isPlayerCorrect(PlayerGameInfo playerGameInfo);
    public abstract CheckResult isTeamCorrect(Team team);
    public abstract CheckResult isTournamentCreateCorrect(Tournament tournament);
    public abstract CheckResult isTournamentStartCorrect(Tournament tournament);
}