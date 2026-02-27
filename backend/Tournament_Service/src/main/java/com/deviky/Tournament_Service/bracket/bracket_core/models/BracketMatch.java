package com.deviky.Tournament_Service.bracket.bracket_core.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BracketMatch {
    Long matchId;
    List<BracketSlot> slots;
}
