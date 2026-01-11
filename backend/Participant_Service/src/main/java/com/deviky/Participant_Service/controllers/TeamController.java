package com.deviky.Participant_Service.controllers;

import com.deviky.Participant_Service.dto.ApiResponse;
import com.deviky.Participant_Service.dto.CreateTeamRequest;
import com.deviky.Participant_Service.dto.TeamDto;
import com.deviky.Participant_Service.services.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/participant/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;

    private Long getSelfPlayerIdFromHeader(String header) {
        try {
            return Long.parseLong(header);
        } catch (Exception e) {
            throw new RuntimeException("Invalid X-Player-Id header");
        }
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TeamDto>> createTeam(@RequestHeader("X-Player-Id") String selfId,
                                                           @RequestBody CreateTeamRequest request) {
        ApiResponse<TeamDto> response = teamService.createTeam(request, getSelfPlayerIdFromHeader(selfId));
        return ResponseEntity.status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK).body(response);
    }

    @PostMapping("/{teamId}/join")
    public ResponseEntity<ApiResponse<TeamDto>> joinTeam(@RequestHeader("X-Player-Id") String selfId,
                                                         @PathVariable Long teamId) {
        ApiResponse<TeamDto> response = teamService.addPlayerToTeam(teamId, getSelfPlayerIdFromHeader(selfId));
        return ResponseEntity.status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK).body(response);
    }

    @PostMapping("/{teamId}/request")
    public ResponseEntity<ApiResponse<TeamDto>> handleRequest(@RequestHeader("X-Player-Id") String selfId,
                                                              @PathVariable Long teamId,
                                                              @RequestParam Long playerId,
                                                              @RequestParam boolean approve) {
        ApiResponse<TeamDto> response = teamService.handleRequest(teamId, playerId, getSelfPlayerIdFromHeader(selfId), approve);
        return ResponseEntity.status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK).body(response);
    }

    @PostMapping("/{teamId}/invite")
    public ResponseEntity<String> generateInvite(@PathVariable Long teamId) {
        String link = teamService.generateInviteLink(teamId);
        return ResponseEntity.ok(link);
    }

    @PostMapping("/join-by-token")
    public ResponseEntity<ApiResponse<TeamDto>> joinByToken(@RequestHeader("X-Player-Id") String selfId,
                                                            @RequestParam String token) {
        ApiResponse<TeamDto> response = teamService.joinByInviteToken(token, getSelfPlayerIdFromHeader(selfId));
        return ResponseEntity.status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{teamId}/leave")
    public ResponseEntity<ApiResponse<TeamDto>> leaveTeam(@RequestHeader("X-Player-Id") String selfId,
                                                          @PathVariable Long teamId) {
        ApiResponse<TeamDto> response = teamService.removePlayerFromTeam(teamId, getSelfPlayerIdFromHeader(selfId),
                getSelfPlayerIdFromHeader(selfId), false);
        return ResponseEntity.status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK).body(response);
    }

    @DeleteMapping("/{teamId}/remove")
    public ResponseEntity<ApiResponse<TeamDto>> removePlayer(@RequestHeader("X-Player-Id") String selfId,
                                                             @PathVariable Long teamId,
                                                             @RequestParam Long playerId) {
        ApiResponse<TeamDto> response = teamService.removePlayerFromTeam(teamId, playerId, getSelfPlayerIdFromHeader(selfId), true);
        return ResponseEntity.status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK).body(response);
    }

    @PostMapping("/{teamId}/transfer-captain")
    public ResponseEntity<ApiResponse<TeamDto>> transferCaptain(@RequestHeader("X-Player-Id") String selfId,
                                                                @PathVariable Long teamId,
                                                                @RequestParam Long newCaptainId) {
        ApiResponse<TeamDto> response = teamService.transferCaptain(teamId, newCaptainId, getSelfPlayerIdFromHeader(selfId));
        return ResponseEntity.status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK).body(response);
    }
}

