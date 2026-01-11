package com.deviky.Participant_Service.repositories;

import com.deviky.Participant_Service.models.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findByGameId(Integer gameId);
    List<Team> findByNameContainingIgnoreCase(String substring);
}
