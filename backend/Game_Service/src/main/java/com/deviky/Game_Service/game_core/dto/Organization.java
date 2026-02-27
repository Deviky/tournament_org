package com.deviky.Game_Service.game_core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Organization {
    Long id;
    String organizerName;
    String description;
}
