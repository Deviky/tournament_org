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
public class ParticipantClientService {
    private final WebClient.Builder webClientBuilder;
    private WebClient getWebClient() {
        return webClientBuilder.baseUrl("http://localhost:8070").build();
    }

    public ApiResponse<List<Team>> getTeams(List<Long> teamIds) {
        try {
            return getWebClient().post()  // Используем POST вместо GET
                    .uri("api/participant/teams/get/teams/byIds")
                    .bodyValue(teamIds)  // Отправляем список в теле запроса
                    .retrieve()
                    .onStatus(HttpStatusCode::isError,
                            response -> response.bodyToMono(String.class)
                                    .map(msg -> new RuntimeException("Ошибка при получении команд: " + msg)))
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<List<Team>>>() {})
                    .block();
        } catch (Exception e) {
            return new ApiResponse<>("Ошибка сервера: " + e.getMessage(), null, true);
        }
    }
}
