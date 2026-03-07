package com.deviky.Match_Service.controllers;

import com.deviky.Match_Service.dto.*;
import com.deviky.Match_Service.services.MatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/matches")
@RequiredArgsConstructor
public class MatchController {

    private final MatchService matchService;

    // ==============================
    // 🎮 Создание матчей
    // ==============================

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<MatchDto>> createMatch(
            @RequestHeader("X-User-Id") Long organizerId,
            @RequestBody CreateMatchDto createMatchDto) {

        ApiResponse<MatchDto> response =
                matchService.createMatch(createMatchDto, organizerId);

        return ResponseEntity
                .status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/create_by_batch")
    public ResponseEntity<ApiResponse<Map<Long, MatchDto>>> createMatchesByBracket(
            @RequestBody Map<Long, CreateMatchDto> mapMatchesDto) {

        ApiResponse<Map<Long, MatchDto>> response =
                matchService.createMatchesByBracket(mapMatchesDto);

        return ResponseEntity
                .status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    // ==============================
    // 📋 Получение матчей
    // ==============================

    @GetMapping("/{matchId}")
    public ResponseEntity<ApiResponse<MatchDto>> getMatch(
            @PathVariable Long matchId) {

        ApiResponse<MatchDto> response =
                matchService.getMatch(matchId);

        return ResponseEntity
                .status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/get_by_tournament/{tournamentId}")
    public ResponseEntity<ApiResponse<List<MatchDto>>> getMatchesByTournament(
            @PathVariable Long tournamentId) {

        ApiResponse<List<MatchDto>> response =
                matchService.getMatchesByTournament(tournamentId);

        return ResponseEntity
                .status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    // ==============================
    // 🔄 Обновление матча
    // ==============================

    @PutMapping
    public ResponseEntity<ApiResponse<Void>> updateMatch(
            @RequestHeader("X-User-Id") Long organizerId,
            @RequestBody UpdateMatchDto updateMatchDto) {

        ApiResponse<Void> response =
                matchService.updateMatch(updateMatchDto, organizerId);

        return ResponseEntity
                .status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    // ==============================
    // ⚡ Управление статусом матча
    // ==============================

    @PostMapping("/{matchId}/start")
    public ResponseEntity<ApiResponse<Void>> startMatch(
            @RequestHeader("X-User-Id") Long organizerId,
            @PathVariable Long matchId) {

        ApiResponse<Void> response =
                matchService.startMatch(matchId, organizerId);

        return ResponseEntity
                .status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/{matchId}/finish")
    public ResponseEntity<ApiResponse<Void>> finishMatch(
            @RequestHeader("X-User-Id") Long organizerId,
            @PathVariable Long matchId) {

        ApiResponse<Void> response =
                matchService.finishMatch(matchId, organizerId);

        return ResponseEntity
                .status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/{matchId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelMatch(
            @RequestHeader("X-User-Id") Long organizerId,
            @PathVariable Long matchId) {

        ApiResponse<Void> response =
                matchService.cancelMatch(matchId, organizerId);

        return ResponseEntity
                .status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    // ==============================
    // 🏆 Управление матчами турнира
    // ==============================

    @PostMapping("/by_tournament/{tournamentId}/cancel")
    public ResponseEntity<ApiResponse<Void>> cancelTournamentMatches(
            @RequestHeader("X-User-Id") Long organizerId,
            @PathVariable Long tournamentId) {

        ApiResponse<Void> response =
                matchService.cancelTournamentMatches(tournamentId, organizerId);

        return ResponseEntity
                .status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }
}
