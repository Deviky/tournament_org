package com.deviky.Game_Service.game_core.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckResult {
    boolean isError;
    String message;
}
