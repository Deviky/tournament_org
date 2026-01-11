package com.deviky.Participant_Service.models;

import com.fasterxml.jackson.databind.JsonNode;
import io.lettuce.core.json.JsonType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.Type;
import org.hibernate.type.SqlTypes;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(schema = "participant", name = "player_profile")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Player {
    @Id
    private Long id;

    @Column(nullable = false, length = 50)
    private String nickname;

    @ElementCollection
    @CollectionTable(
            schema = "participant",
            name = "player_games",
            joinColumns = @JoinColumn(name = "player_id")
    )
    @OrderBy("gameId ASC")
    private List<PlayerGameInfo> games = new ArrayList<>();
}
