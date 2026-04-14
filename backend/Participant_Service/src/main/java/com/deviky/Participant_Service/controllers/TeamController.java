package com.deviky.Participant_Service.controllers;

import com.deviky.Participant_Service.dto.ApiResponse;
import com.deviky.Participant_Service.dto.CreateTeamRequest;
import com.deviky.Participant_Service.dto.TeamDto;
import com.deviky.Participant_Service.services.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/participant/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    @PostMapping("/player/create")
    public ResponseEntity<ApiResponse<TeamDto>> createTeam(@RequestHeader("X-Player-Id") Long selfId,
                                                           @RequestBody CreateTeamRequest request) {
        ApiResponse<TeamDto> response = teamService.createTeam(request, selfId);
        return ResponseEntity.status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK).body(response);
    }

    @PostMapping("/player/join/{teamId}")
    public ResponseEntity<ApiResponse<TeamDto>> joinTeam(@RequestHeader("X-Player-Id") Long selfId,
                                                         @PathVariable Long teamId) {
        ApiResponse<TeamDto> response = teamService.addPlayerToTeam(teamId, selfId);
        return ResponseEntity.status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK).body(response);
    }

    @PostMapping("/player/request/{teamId}")
    public ResponseEntity<ApiResponse<TeamDto>> handleRequest(@RequestHeader("X-Player-Id") Long selfId,
                                                              @PathVariable Long teamId,
                                                              @RequestParam Long playerId,
                                                              @RequestParam boolean approve) {
        ApiResponse<TeamDto> response = teamService.handleRequest(teamId, playerId, selfId, approve);
        return ResponseEntity.status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK).body(response);
    }

    @PostMapping("/player/invite_generate/{teamId}")
    public ResponseEntity<String> generateInvite(@PathVariable Long teamId) {
        String link = teamService.generateInviteLink(teamId);
        return ResponseEntity.ok(link);
    }

    @PostMapping("/player/join_by_token")
    public ResponseEntity<ApiResponse<TeamDto>> joinByToken(@RequestHeader("X-Player-Id") Long selfId,
                                                            @RequestParam String token) {
        ApiResponse<TeamDto> response = teamService.joinByInviteToken(token, selfId);
        return ResponseEntity.status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK).body(response);
    }

    @DeleteMapping("/player/leave/{teamId}")
    public ResponseEntity<ApiResponse<TeamDto>> leaveTeam(@RequestHeader("X-Player-Id") Long selfId,
                                                          @PathVariable Long teamId) {
        ApiResponse<TeamDto> response = teamService.removePlayerFromTeam(teamId, selfId, selfId, false);
        return ResponseEntity.status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK).body(response);
    }

    @DeleteMapping("/player/remove/{teamId}")
    public ResponseEntity<ApiResponse<TeamDto>> removePlayer(@RequestHeader("X-Player-Id") Long selfId,
                                                             @PathVariable Long teamId,
                                                             @RequestParam Long playerId) {
        ApiResponse<TeamDto> response = teamService.removePlayerFromTeam(teamId, playerId, selfId, true);
        return ResponseEntity.status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK).body(response);
    }

    @PostMapping("/player/transfer_captain/{teamId}")
    public ResponseEntity<ApiResponse<TeamDto>> transferCaptain(@RequestHeader("X-Player-Id") Long selfId,
                                                                @PathVariable Long teamId,
                                                                @RequestParam Long newCaptainId) {
        ApiResponse<TeamDto> response = teamService.transferCaptain(teamId, newCaptainId, selfId);
        return ResponseEntity.status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK).body(response);
    }

    @GetMapping("/public/get/{teamId}")
    public ResponseEntity<ApiResponse<TeamDto>> getTeam(@PathVariable Long teamId) {
        ApiResponse<TeamDto> response = teamService.getTeamWithPlayers(teamId);
        return ResponseEntity.status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK).body(response);
    }

    @GetMapping("/public/get_by_ids")
    public ResponseEntity<ApiResponse<List<TeamDto>>> getTeamsByIds(@RequestBody List<Long> teamId) {
        ApiResponse<List<TeamDto>> response = teamService.getTeamsWithPlayersByIds(teamId);
        return ResponseEntity.status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK).body(response);
    }
}

