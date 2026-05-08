package com.deviky.Participant_Service.dto;

import com.deviky.Participant_Service.models.PlayerGameInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdatePlayerRequest {
    private String nickname;
    private List<PlayerGameInfo> games;
}
