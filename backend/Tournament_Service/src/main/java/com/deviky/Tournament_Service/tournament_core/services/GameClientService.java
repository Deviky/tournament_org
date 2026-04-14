package com.deviky.Tournament_Service.tournament_core.services;


import com.deviky.Tournament_Service.tournament_core.dto.ApiResponse;
import com.deviky.Tournament_Service.tournament_core.dto.Game;
import com.deviky.Tournament_Service.tournament_core.dto.Team;
import com.deviky.Tournament_Service.tournament_core.dto.TournamentDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GameClientService {
    private final WebClient.Builder webClientBuilder;
    private WebClient getWebClient() {
        return webClientBuilder.baseUrl("http://GAME-SERVICE").build();
    }

    public ApiResponse<Game> getGame(int gameId) {
        try {
            return getWebClient().get()
                    .uri("/api/game/public/get/" + gameId)
                    .retrieve()
                    .onStatus(HttpStatusCode::isError,
                            response -> response.bodyToMono(String.class)
                                    .flatMap(msg -> {
                                        try {
                                            ObjectMapper mapper = new ObjectMapper();
                                            ApiResponse<?> apiResponse = mapper.readValue(msg, new TypeReference<ApiResponse<?>>() {});
                                            return Mono.error(new RuntimeException(apiResponse.getMessage()));
                                        } catch (Exception e) {
                                            // если JSON не парсится — оставляем сырое сообщение
                                            return Mono.error(new RuntimeException(msg));
                                        }
                                    }))
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<Game>>() {})
                    .block();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при получении игры: " + e.getMessage());
        }
    }

    public ApiResponse<Void> checkTournamentCreate(TournamentDto tournament) {
        try {
            return getWebClient().post()
                    .uri("/api/game/private/check_tournament_create")
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
                    .uri("/api/game/private/check_tournament_start")
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
                    .uri("/api/game/private/get_bracket_algorithms/" + gameId)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<List<String>>>() {})
                    .block();
            return response;
        } catch (Exception e) {
            return new ApiResponse<>("Ошибка сервера: " + e.getMessage(), null, true);
        }
    }
}
