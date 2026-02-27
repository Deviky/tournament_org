package com.deviky.Tournament_Service.bracket.bracket_core.models;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Bracket {
    String version;
    String algorithmType;
    List<BracketGroup> bracketGroups;

    public Bracket(String json) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            Bracket temp = objectMapper.readValue(json, Bracket.class);
            this.version = temp.version;
            this.algorithmType = temp.algorithmType;
            this.bracketGroups = temp.bracketGroups;
        } catch (IOException e) {
            throw new RuntimeException("Ошибка десериализации JSON в Bracket", e);
        }
    }

    public String toJsonStr() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка сериализации Bracket в JSON", e);
        }
    }
}
