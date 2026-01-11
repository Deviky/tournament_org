package com.deviky.Game_Service.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(schema = "game", name = "game_params")
public class GameParam {
    @Id
    @Column(name = "param_name")
    String paramName;
    @Column(name = "param_value")
    String paramValue;
    @Column(name = "apply_rn")
    int applyRn;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private GameEntity game;
}
