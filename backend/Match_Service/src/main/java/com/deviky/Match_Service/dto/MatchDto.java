package com.deviky.Match_Service.dto;

import com.deviky.Match_Service.models.MatchStatus;
import jakarta.annotation.Nonnull;
import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MatchDto {
    Long id;
    Long tournamentId;
    MatchStatus status;
    String links;
    LocalDateTime startAt;
    LocalDateTime endAt;
    List<MatchTeamDto> teams;
}
