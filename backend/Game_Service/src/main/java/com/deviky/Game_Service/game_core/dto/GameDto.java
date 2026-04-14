package com.deviky.Game_Service.game_core.dto;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class GameDto {
    Integer id;
    String name;
    String description;
}
