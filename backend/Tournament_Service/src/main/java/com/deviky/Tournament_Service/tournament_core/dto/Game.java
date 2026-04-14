package com.deviky.Tournament_Service.tournament_core.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Game {
    Integer id;
    String name;
    String description;
}

