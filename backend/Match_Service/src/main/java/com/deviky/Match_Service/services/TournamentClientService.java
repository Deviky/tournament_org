package com.deviky.Match_Service.services;

import com.deviky.Match_Service.dto.ApiResponse;
import com.deviky.Match_Service.dto.Team;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TournamentClientService {
    private final WebClient.Builder webClientBuilder;

    private WebClient getWebClient() {
        return webClientBuilder.baseUrl("http://localhost:8070").build();
    }

    public ApiResponse<Void> checkTournamentInfo(Long tournamentId, Long organizerId) {
        try {
            return getWebClient().get()  // Используем GET, как на сервере
                    .uri(uriBuilder -> uriBuilder
                            .path("api/torunaments/" + tournamentId + "/create_match_check")
                            .queryParam("organizerId", organizerId)  // Передаем как query параметр
                            .build())
                    .retrieve()
                    .onStatus(HttpStatusCode::isError,
                            response -> response.bodyToMono(String.class)
                                    .map(msg -> new RuntimeException("Ошибка при проверке турнира: " + msg)))
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<Void>>() {})
                    .block();
        } catch (Exception e) {
            return new ApiResponse<>("Ошибка сервера: " + e.getMessage(), null, true);
        }
    }
}
