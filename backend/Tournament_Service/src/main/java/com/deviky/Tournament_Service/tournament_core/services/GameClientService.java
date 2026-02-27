package com.deviky.Tournament_Service.tournament_core.services;


import com.deviky.Tournament_Service.tournament_core.dto.ApiResponse;
import com.deviky.Tournament_Service.tournament_core.dto.Game;
import com.deviky.Tournament_Service.tournament_core.dto.Team;
import com.deviky.Tournament_Service.tournament_core.dto.TournamentDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GameClientService {
    private final WebClient.Builder webClientBuilder;
    private WebClient getWebClient() {
        return webClientBuilder.baseUrl("http://localhost:8071").build();
    }

    public Game getGame(int gameId) {
        try {
            return getWebClient().get()
                    .uri("/api/game/" + gameId)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError,
                            response -> response.bodyToMono(String.class)
                                    .map(msg -> new RuntimeException("Ошибка при получении игры: " + msg)))
                    .bodyToMono(Game.class)
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при получении игры: " + e.getMessage());
        }
    }

    public ApiResponse<Void> checkTournamentCreate(TournamentDto tournament) {
        try {
            return getWebClient().post()
                    .uri("/api/game/check-tournament-create")
                    .bodyValue(tournament)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<Void>>() {})
                    .block();
        } catch (Exception e) {
            return new ApiResponse<>("Ошибка сервера: " + e.getMessage(), null, true);
        }
    }

    public ApiResponse<Void> checkTournamentStart(TournamentDto tournament) {
        try {
            return getWebClient().post()
                    .uri("/api/game/check-tournament-start")
                    .bodyValue(tournament)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<Void>>() {})
                    .block();
        } catch (Exception e) {
            return new ApiResponse<>("Ошибка сервера: " + e.getMessage(), null, true);
        }
    }

    public ApiResponse<List<String>> getBracketAlgorithms(Integer gameId) {
        try {
            ApiResponse<List<String>> response = getWebClient()
                    .get()
                    .uri("/api/game/get-bracket-algorithms/{gameId}", gameId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<List<String>>>() {})
                    .block();
            return response;
        } catch (Exception e) {
            return new ApiResponse<>("Ошибка сервера: " + e.getMessage(), null, true);
        }
    }

    public ApiResponse<Void> checkTeamCorrect(Team team) {
        try {
            return getWebClient().post()
                    .uri("/api/game/check-team")
                    .bodyValue(team)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<Void>>() {})
                    .block();
        } catch (Exception e) {
            return new ApiResponse<>("Ошибка сервера: " + e.getMessage(), null, true);
        }
    }
}
