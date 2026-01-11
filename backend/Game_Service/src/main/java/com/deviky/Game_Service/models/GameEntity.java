package com.deviky.Game_Service.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(schema = "game", name = "game")
public class GameEntity {
    @Id
    @Column(name = "id")
    int id;
    @Column(name = "name")
    String name;
    @Column(name = "description")
    String description;
}
