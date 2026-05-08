package com.deviky.Participant_Service.controllers;

import com.deviky.Participant_Service.dto.ApiResponse;
import com.deviky.Participant_Service.dto.CreatePlayerRequest;
import com.deviky.Participant_Service.dto.PlayerDto;
import com.deviky.Participant_Service.dto.SearchDto;
import com.deviky.Participant_Service.dto.TeamDto;
import com.deviky.Participant_Service.dto.UpdatePlayerRequest;
import com.deviky.Participant_Service.services.PlayerService;
import com.deviky.Participant_Service.services.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/participant/players")
@RequiredArgsConstructor
public class PlayerController {

    private final PlayerService playerService;
    private final TeamService teamService;

    @PostMapping("/private/create")
    public ResponseEntity<ApiResponse<PlayerDto>> createPlayer(
            @RequestBody CreatePlayerRequest dto
    ) {
        ApiResponse<PlayerDto> response = playerService.createPlayer(dto);
        HttpStatus status = response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    @PutMapping("/private/update")
    public ResponseEntity<ApiResponse<PlayerDto>> updatePlayer(
            @RequestHeader("X-User-Id") Long selfId,
            @RequestBody UpdatePlayerRequest dto
    ) {
        ApiResponse<PlayerDto> response = playerService.updatePlayer(selfId, dto);
        HttpStatus status = response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/public/{playerId}")
    public ResponseEntity<ApiResponse<PlayerDto>> getPlayer(
            @PathVariable Long playerId
    ) {
        ApiResponse<PlayerDto> response = playerService.getPlayerWithTeams(playerId);
        HttpStatus status = response.isError() ? HttpStatus.NOT_FOUND : HttpStatus.OK;
        return ResponseEntity.status(status).body(response);
    }

    @GetMapping("/public/search")
    public ResponseEntity<ApiResponse<SearchDto>> searchPlayers(
            @RequestParam String query
    ) {
        ApiResponse<List<PlayerDto>> responsePlayer = playerService.searchPlayers(query);
        ApiResponse<List<TeamDto>> responseTeam = teamService.searchTeams(query);
        HttpStatus status =
                responsePlayer.isError() || responseTeam.isError()
                        ? HttpStatus.BAD_REQUEST
                        : HttpStatus.OK;
        SearchDto searchDto = new SearchDto(responsePlayer.getData(), responseTeam.getData());
        return ResponseEntity.status(status).body(new ApiResponse<>(
                responsePlayer.isError() || responseTeam.isError() ? "Ошибка сервера" : "",
                searchDto,
                responsePlayer.isError() || responseTeam.isError()
        ));
    }
}
