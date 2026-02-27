package com.deviky.Tournament_Service.tournament_core.repositories;

import com.deviky.Tournament_Service.tournament_core.models.TournamentTeam;
import com.deviky.Tournament_Service.tournament_core.models.TournamentTeamId;
import org.apache.el.stream.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TournamentTeamRepository extends JpaRepository<TournamentTeam, TournamentTeamId> {
}
