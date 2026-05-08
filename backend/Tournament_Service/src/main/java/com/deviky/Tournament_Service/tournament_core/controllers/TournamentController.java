package com.deviky.Tournament_Service.tournament_core.controllers;

import com.deviky.Tournament_Service.bracket.bracket_core.models.*;
import com.deviky.Tournament_Service.tournament_core.dto.*;
import com.deviky.Tournament_Service.tournament_core.services.TournamentService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tournaments")
@RequiredArgsConstructor
public class TournamentController {

    private final TournamentService tournamentService;

    // ==============================
    // 🎮 Алгоритмы сетки
    // ==============================

    @GetMapping("/organizer/games/{gameId}/algorithms")
    public ResponseEntity<ApiResponse<Map<String, ObjectNode>>> getAlgorithms(
            @RequestHeader("X-User-Id") Long organizerId,
            @PathVariable Integer gameId) {

        ApiResponse<Map<String, ObjectNode>> response =
                tournamentService.getAlgorithms(gameId);

        return ResponseEntity
                .status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    // ==============================
    // 🏆 Создание / обновление турнира
    // ==============================

    @PostMapping("/organizer/create")
    public ResponseEntity<ApiResponse<TournamentDto>> createTournament(
            @RequestHeader("X-User-Id") Long organizerId,
            @RequestBody TournamentCreateDto dto) {

        ApiResponse<TournamentDto> response =
                tournamentService.createTournament(dto, organizerId);

        return ResponseEntity
                .status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @PutMapping("/organizer/update/{tournamentId}")
    public ResponseEntity<ApiResponse<TournamentDto>> updateTournament(
            @RequestHeader("X-User-Id") Long organizerId,
            @PathVariable Long tournamentId,
            @RequestBody TournamentUpdateDto dto) {

        ApiResponse<TournamentDto> response =
                tournamentService.updateTournament(tournamentId, organizerId, dto);

        return ResponseEntity
                .status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    // ==============================
    // 📋 Получение турниров
    // ==============================

    @GetMapping("/public/get_all")
    public ResponseEntity<ApiResponse<List<TournamentDto>>> getAll() {

        ApiResponse<List<TournamentDto>> response =
                tournamentService.getTournaments();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/public/get/{tournamentId}")
    public ResponseEntity<ApiResponse<TournamentDto>> get(@PathVariable Long tournamentId) {

        ApiResponse<TournamentDto> response =
                tournamentService.getTournament(tournamentId);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/organizer/teams/{tournamentId}")
    public ResponseEntity<ApiResponse<List<TournamentTeamEntryDto>>> getTournamentTeamEntries(
            @RequestHeader("X-User-Id") Long organizerId,
            @PathVariable Long tournamentId) {

        ApiResponse<List<TournamentTeamEntryDto>> response =
                tournamentService.getTournamentTeamEntries(tournamentId, organizerId);

        return ResponseEntity
                .status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/public/get_by_game/{gameId}")
    public ResponseEntity<ApiResponse<List<TournamentDto>>> getByGame(
            @PathVariable Integer gameId) {

        ApiResponse<List<TournamentDto>> response =
                tournamentService.getTournamentsByGame(gameId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/public/get_by_games")
    public ResponseEntity<ApiResponse<List<TournamentDto>>> getByGames(
            @RequestBody List<Integer> gameIds) {

        ApiResponse<List<TournamentDto>> response =
                tournamentService.getTournamentsByGames(gameIds);

        return ResponseEntity.ok(response);
    }

    // ==============================
    // 🧩 Генерация сетки
    // ==============================

    @PostMapping("/organizer/bracket/generate/{tournamentId}")
    public ResponseEntity<ApiResponse<Bracket>> generateBracket(
            @RequestHeader("X-User-Id") Long organizerId,
            @PathVariable Long tournamentId,
            @RequestBody TournamentGenerateBracketDto dto) {

        ApiResponse<Bracket> response =
                tournamentService.generateBracket(tournamentId, organizerId, dto);

        return ResponseEntity
                .status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/organizer/bracket/final/{tournamentId}")
    public ResponseEntity<ApiResponse<Void>> createFinalBracket(
            @RequestHeader("X-User-Id") Long organizerId,
            @PathVariable Long tournamentId,
            @RequestBody Bracket bracket) {

        ApiResponse<Void> response =
                tournamentService.submitFinalBracket(tournamentId, organizerId, bracket);

        return ResponseEntity
                .status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/organizer/bracket/update/{tournamentId}")
    public ResponseEntity<ApiResponse<Void>> updateBracketByMatchResult(
            @PathVariable Long tournamentId,
            @RequestBody MatchResult matchResult) {

        ApiResponse<Void> response =
                tournamentService.updateBracketByMatchResult(tournamentId, matchResult);

        return ResponseEntity
                .status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/private/bracket/match_cancel/{tournamentId}")
    public ApiResponse<Void> cancelMatchUpdateBracket(
            @PathVariable Long tournamentId,
            @RequestParam Long matchId
    ) {
        return tournamentService.updateBracketAfterMatchCancel(tournamentId, matchId);
    }

    // ==============================
    // 👥 Регистрация команды
    // ==============================

    @PostMapping("/player/register/{tournamentId}")
    public ResponseEntity<ApiResponse<Void>> registerTeam(
            @RequestHeader("X-User-Id") Long playerId,
            @PathVariable Long tournamentId,
            @RequestParam Long teamId,
            @RequestParam(required = false) String inviteToken) {

        ApiResponse<Void> response =
                tournamentService.registerTeam(
                        tournamentId,
                        teamId,
                        playerId,
                        inviteToken
                );

        return ResponseEntity
                .status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    // ==============================
    // 🔐 Invite link
    // ==============================

    @PostMapping("/organizer/invite/{tournamentId}")
    public ResponseEntity<ApiResponse<String>> generateInvite(
            @RequestHeader("X-User-Id") Long organizerId,
            @PathVariable Long tournamentId) {

        ApiResponse<String> response =
                tournamentService.generateInviteLink(tournamentId, organizerId);

        return ResponseEntity
                .status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    // ==============================
    // 🚪 Выход / кик / апрув
    // ==============================

    @DeleteMapping("/player/leave/{tournamentId}/teams/{teamId}")
    public ResponseEntity<ApiResponse<String>> leaveTeam(
            @RequestHeader("X-User-Id") Long playerId,
            @PathVariable Long tournamentId,
            @PathVariable Long teamId) {

        ApiResponse<String> response =
                tournamentService.leaveTeam(tournamentId, teamId, playerId);

        return ResponseEntity
                .status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @DeleteMapping("/organizer/kick/{tournamentId}/teams/{teamId}")
    public ResponseEntity<ApiResponse<String>> kickTeam(
            @RequestHeader("X-User-Id") Long organizerId,
            @PathVariable Long tournamentId,
            @PathVariable Long teamId) {

        ApiResponse<String> response =
                tournamentService.kickTeam(tournamentId, teamId, organizerId);

        return ResponseEntity
                .status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/organizer/handle/{tournamentId}/teams/{teamId}")
    public ResponseEntity<ApiResponse<String>> handleRequest(
            @RequestHeader("X-User-Id") Long organizerId,
            @PathVariable Long tournamentId,
            @PathVariable Long teamId,
            @RequestParam boolean approve) {

        ApiResponse<String> response =
                tournamentService.handleRequestTeamTournament(
                        tournamentId,
                        teamId,
                        organizerId,
                        approve
                );

        return ResponseEntity
                .status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/organizer/start_registration/{tournamentId}")
    public ResponseEntity<ApiResponse<String>> startRegistrationTournament(
            @RequestHeader("X-User-Id") Long organizerId,
            @PathVariable Long tournamentId) {

        ApiResponse<String> response =
                tournamentService.startRegistrationTournament(tournamentId, organizerId);

        return ResponseEntity
                .status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/organizer/close_registration/{tournamentId}")
    public ResponseEntity<ApiResponse<String>> closeRegistrationTournament(
            @RequestHeader("X-User-Id") Long organizerId,
            @PathVariable Long tournamentId) {

        ApiResponse<String> response =
                tournamentService.closeRegistrationTournament(tournamentId, organizerId);

        return ResponseEntity
                .status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/organizer/start/{tournamentId}")
    public ResponseEntity<ApiResponse<String>> startTournament(
            @RequestHeader("X-User-Id") Long organizerId,
            @PathVariable Long tournamentId) {

        ApiResponse<String> response =
                tournamentService.startTournament(tournamentId, organizerId);

        return ResponseEntity
                .status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/organizer/end/{tournamentId}")
    public ResponseEntity<ApiResponse<String>> endTournament(
            @RequestHeader("X-User-Id") Long organizerId,
            @PathVariable Long tournamentId) {

        ApiResponse<String> response =
                tournamentService.endTournament(tournamentId, organizerId);

        return ResponseEntity
                .status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/moderator/ban/{tournamentId}")
    public ResponseEntity<ApiResponse<String>> banTournament(
            @PathVariable Long tournamentId) {

        ApiResponse<String> response =
                tournamentService.banTournament(tournamentId);

        return ResponseEntity
                .status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/organizer/cancel/{tournamentId}")
    public ApiResponse<String> cancelTournament(
            @RequestHeader("X-User-Id") Long organizerId,
            @PathVariable Long tournamentId
    ) {
        return tournamentService.cancelTournament(tournamentId, organizerId);
    }

    @GetMapping("/private/create_match_check/{tournamentId}")
    public ResponseEntity<ApiResponse<Void>> checkTournamentCreateMatch(
            @PathVariable Long tournamentId,
            @RequestParam Long organizerId) {

        ApiResponse<Void> response =
                tournamentService.checkTournamentMatchCreate(tournamentId, organizerId);

        return ResponseEntity
                .status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }
}
