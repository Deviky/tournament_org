package com.deviky.Participant_Service.repositories;

import com.deviky.Participant_Service.models.Player;
import com.deviky.Participant_Service.models.Team;
import com.deviky.Participant_Service.models.TeamPlayer;
import com.deviky.Participant_Service.models.TeamPlayerId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    List<Player> findByNicknameContainingIgnoreCase(String substring);
}

