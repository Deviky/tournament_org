package com.deviky.Match_Service.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateMatchDto {
    Long matchId;
    String links;
    LocalDateTime startAt;
    LocalDateTime endAt;
}
