package com.deviky.Tournament_Service.bracket.bracket_core.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class BracketGroup {
    String name;
    List<BracketMatch> matches;
}
