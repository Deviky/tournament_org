package com.deviky.Game_Service.game_core.repositories;

import com.deviky.Game_Service.game_core.models.GameEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GameRepository extends JpaRepository<GameEntity, Integer> {
}
