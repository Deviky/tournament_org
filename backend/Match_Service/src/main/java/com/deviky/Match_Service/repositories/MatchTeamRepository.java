package com.deviky.Match_Service.repositories;

import com.deviky.Match_Service.models.MatchTeam;
import com.deviky.Match_Service.models.MatchTeamId;
import com.deviky.Match_Service.models.MatchTeamResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchTeamRepository extends JpaRepository<MatchTeam, MatchTeamId> {
}
