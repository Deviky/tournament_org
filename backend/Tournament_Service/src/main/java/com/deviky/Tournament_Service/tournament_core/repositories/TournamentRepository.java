package com.deviky.Tournament_Service.tournament_core.repositories;

import com.deviky.Tournament_Service.tournament_core.models.Tournament;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TournamentRepository extends JpaRepository<Tournament, Long> {
    List<Tournament> findByGameId(Integer gameId);
    List<Tournament> findByGameIdIn(List<Integer> gameIds);
}
