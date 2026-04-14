package com.deviky.Participant_Service.services;

import com.deviky.Participant_Service.dto.ApiResponse;
import com.deviky.Participant_Service.dto.Game;
import com.deviky.Participant_Service.dto.TeamDto;
import com.deviky.Participant_Service.models.Player;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

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

    // Проверка игрока через Game Service
    public ApiResponse<Void> checkPlayer(Player player) {
        try {
            return getWebClient().post()
                    .uri("/api/game/private/check_player")
                    .bodyValue(player)
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
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<Void>>() {})
                    .block();
        } catch (Exception e) {
            return new ApiResponse<>(e.getMessage(), null, true);
        }
    }

    public ApiResponse<Void> checkTeamCorrect(TeamDto team) {
        try {
            return getWebClient().post()
                    .uri("/api/game/private/check_team")
                    .bodyValue(team)
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
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<Void>>() {})
                    .block();
        } catch (Exception e) {
            return new ApiResponse<>("Ошибка сервера: " + e.getMessage(), null, true);
        }
    }

}
