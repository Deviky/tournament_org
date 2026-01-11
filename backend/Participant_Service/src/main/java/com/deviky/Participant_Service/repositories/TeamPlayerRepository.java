package com.deviky.Participant_Service.repositories;

import com.deviky.Participant_Service.models.TeamPlayer;
import com.deviky.Participant_Service.models.TeamPlayerId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamPlayerRepository extends JpaRepository<TeamPlayer, TeamPlayerId> {
    List<TeamPlayer> findByTeamId(Long teamId);

    List<TeamPlayer> findByPlayerId(Long playerId);

    Optional<TeamPlayer> findByPlayerIdAndTeam_GameId(Long playerId, Integer gameId);

    void deleteByPlayerId(Long playerId);
}
