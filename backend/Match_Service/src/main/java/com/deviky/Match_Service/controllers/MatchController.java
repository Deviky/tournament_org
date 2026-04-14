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


    @PostMapping("/private/create_by_batch")
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

    @GetMapping("/public/get/{matchId}")
    public ResponseEntity<ApiResponse<MatchDto>> getMatch(
            @PathVariable Long matchId) {

        ApiResponse<MatchDto> response =
                matchService.getMatch(matchId);

        return ResponseEntity
                .status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @GetMapping("/public/get_by_tournament/{tournamentId}")
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

    @PutMapping("/organizer/update")
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

    @PostMapping("/organizer/start/{matchId}")
    public ResponseEntity<ApiResponse<Void>> startMatch(
            @RequestHeader("X-User-Id") Long organizerId,
            @PathVariable Long matchId) {

        ApiResponse<Void> response =
                matchService.startMatch(matchId, organizerId);

        return ResponseEntity
                .status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/organizer/finish")
    public ResponseEntity<ApiResponse<Void>> finishMatch(
            @RequestHeader("X-User-Id") Long organizerId,
            @RequestBody MatchResultDto matchResultDto) {

        ApiResponse<Void> response =
                matchService.finishMatch(matchResultDto, organizerId);

        return ResponseEntity
                .status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }

    @PostMapping("/organizer/cancel/{matchId}")
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

    @PostMapping("/private/by_tournament/cancel/{tournamentId}")
    public ResponseEntity<ApiResponse<Void>> cancelTournamentMatches(
            @PathVariable Long tournamentId) {

        ApiResponse<Void> response =
                matchService.cancelTournamentMatches(tournamentId);

        return ResponseEntity
                .status(response.isError() ? HttpStatus.BAD_REQUEST : HttpStatus.OK)
                .body(response);
    }
}
