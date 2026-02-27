package com.deviky.Tournament_Service.tournament_core.controllers;

import com.deviky.Tournament_Service.bracket.bracket_core.algorithm_base.AlgorithmParams;
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

    @GetMapping("/games/{gameId}/algorithms")
    public ResponseEntity<ApiResponse<Map<String, ObjectNode>>> getAlgorithms(
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

    @PostMapping
    public ResponseEntity<ApiResponse<TournamentDto>> createTournament(
            @RequestBody TournamentCreateDto dto) {

        ApiResponse<TournamentDto> response =
                tournamentService.createTournament(dto);

        return ResponseEntity
                .status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @PutMapping("/{tournamentId}")
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

    @GetMapping
    public ResponseEntity<ApiResponse<List<TournamentDto>>> getAll() {

        ApiResponse<List<TournamentDto>> response =
                tournamentService.getTournaments();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/games/{gameId}")
    public ResponseEntity<ApiResponse<List<TournamentDto>>> getByGame(
            @PathVariable Integer gameId) {

        ApiResponse<List<TournamentDto>> response =
                tournamentService.getTournamentsByGame(gameId);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/games/list")
    public ResponseEntity<ApiResponse<List<TournamentDto>>> getByGames(
            @RequestBody List<Integer> gameIds) {

        ApiResponse<List<TournamentDto>> response =
                tournamentService.getTournamentsByGames(gameIds);

        return ResponseEntity.ok(response);
    }

    // ==============================
    // 🧩 Генерация сетки
    // ==============================

    @PostMapping("/{tournamentId}/bracket/generate")
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

    @PostMapping("/{tournamentId}/bracket/final")
    public ResponseEntity<ApiResponse<Void>> createFinalBracket(
            @RequestHeader("X-User-Id") Long organizerId,
            @PathVariable Long tournamentId,
            @RequestBody Bracket bracket) {

        ApiResponse<Void> response =
                tournamentService.createFinalBracket(tournamentId, organizerId, bracket);

        return ResponseEntity
                .status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/{tournamentId}/bracket/update")
    public ResponseEntity<ApiResponse<Void>> updateBracketByMatchResult(
            @PathVariable Long tournamentId,
            @RequestBody MatchResult matchResult) {

        ApiResponse<Void> response =
                tournamentService.updateBracketByMatchResult(tournamentId, matchResult);

        return ResponseEntity
                .status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    // ==============================
    // 👥 Регистрация команды
    // ==============================

    @PostMapping("/{tournamentId}/register")
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

    @PostMapping("/{tournamentId}/invite")
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

    @DeleteMapping("/{tournamentId}/teams/{teamId}/leave")
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

    @DeleteMapping("/{tournamentId}/teams/{teamId}/kick")
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

    @PostMapping("/{tournamentId}/teams/{teamId}/handle")
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

    @PostMapping("/{tournamentId}/start")
    public ResponseEntity<ApiResponse<String>> startTournament(
            @RequestHeader("X-User-Id") Long organizerId,
            @PathVariable Long tournamentId) {

        ApiResponse<String> response =
                tournamentService.startTournament(tournamentId, organizerId);

        return ResponseEntity
                .status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/{tournamentId}/end")
    public ResponseEntity<ApiResponse<String>> endTournament(
            @RequestHeader("X-User-Id") Long organizerId,
            @PathVariable Long tournamentId) {

        ApiResponse<String> response =
                tournamentService.endTournament(tournamentId, organizerId);

        return ResponseEntity
                .status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/{tournamentId}/ban")
    public ResponseEntity<ApiResponse<String>> banTournament(
            @PathVariable Long tournamentId) {

        ApiResponse<String> response =
                tournamentService.banTournament(tournamentId);

        return ResponseEntity
                .status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }
}
