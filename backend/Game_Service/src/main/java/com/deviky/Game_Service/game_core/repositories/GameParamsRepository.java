package com.deviky.Game_Service.game_core.repositories;

import com.deviky.Game_Service.game_core.models.GameParam;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameParamsRepository extends JpaRepository<GameParam, String>{
    @Query("""
        SELECT gp
        FROM GameParam gp
        JOIN gp.game g
        WHERE g.name = :gameName
    """)
    List<GameParam> getAllGameParams(@Param("game_name") String gameName);
}
