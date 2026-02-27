package com.deviky.Participant_Service.services;

import com.deviky.Participant_Service.dto.ApiResponse;
import com.deviky.Participant_Service.dto.Game;
import com.deviky.Participant_Service.models.Player;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.stereotype.Service;

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

    // Проверка игрока через Game Service
    public ApiResponse<Void> checkPlayer(Player player) {
        try {
            return getWebClient().post()
                    .uri("/api/game/check-player")
                    .bodyValue(player)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<Void>>() {})
                    .block();
        } catch (Exception e) {
            return new ApiResponse<>("Ошибка при проверке игрока: " + e.getMessage(), null, true);
        }
    }

}
