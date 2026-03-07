package com.deviky.Match_Service.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateMatchDto {
    Long tournamentId;
    @Column(name = "start_at")
    LocalDateTime startAt;
    @Column(name = "end_at")
    String links;
    LocalDateTime endAt;
    List<Long> teamIds;
}
