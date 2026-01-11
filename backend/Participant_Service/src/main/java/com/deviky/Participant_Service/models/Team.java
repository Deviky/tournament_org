package com.deviky.Participant_Service.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(schema = "participant", name = "team")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "game_id", nullable = false)
    private Integer gameId;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TeamStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private TeamType type; // PUBLIC / PRIVATE
}