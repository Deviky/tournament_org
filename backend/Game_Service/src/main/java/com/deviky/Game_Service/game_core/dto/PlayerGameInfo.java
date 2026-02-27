package com.deviky.Game_Service.game_core.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlayerGameInfo {
    private Integer gameId;
    private Map<String, String> links;
}
